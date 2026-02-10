package de.kaleidox.galio.feature.roles;

import de.kaleidox.galio.preferences.guild.GuildPreferences;
import de.kaleidox.galio.preferences.guild.ReactionRoleBinding;
import de.kaleidox.galio.preferences.guild.ReactionRoleSet;
import de.kaleidox.galio.repo.GuildPreferenceRepo;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.comroid.annotations.Description;
import org.comroid.commands.Command;
import org.comroid.commands.impl.CommandManager;
import org.comroid.commands.model.CommandError;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Log
@Component
@Command("roles")
@Description("Configure reaction roles")
public class ReactionRoleService extends ListenerAdapter {
    public static final String COMPONENT_ID_ROLESET_EDIT = "roleset-edit-";
    public static final String COMPONENT_ID_ROLE_ADD     = "roles-add-";
    public static final String COMPONENT_ID_ROLE_REMOVE  = "roles-remove-";
    public static final String COMPONENT_ID_ROLE_EDIT    = "roles-edit-";
    public static final String OPTION_ID_ROLE            = "option-role";
    public static final String OPTION_ID_CHANNEL         = "option-channel";
    public static final String OPTION_ID_EMOJI           = "option-emoji";
    public static final String OPTION_ID_NAME            = "option-name";
    public static final String OPTION_ID_DESCRIPTION     = "option-description";

    @Autowired GuildPreferenceRepo guilds;
    @Autowired JDA                 jda;

    @Command(permission = "8")
    @Description("Resend all reaction role messages in this server")
    @SuppressWarnings("UnusedReturnValue")
    public String resend(Guild guild) {
        var prefs = guilds.findById(guild.getIdLong()).orElse(null);
        if (prefs == null || prefs.getRoleSets().isEmpty()) return "There are no configured reaction roles";

        for (var roleSet : prefs.getRoleSets()) {
            var channelId = roleSet.getChannelId();
            var channel   = jda.getTextChannelById(channelId);

            if (channel == null) {
                log.warning("Could not find channel with ID %d for role set %s".formatted(channelId, roleSet));
                continue;
            }

            // send new message
            var msg = roleSet.createMessage();
            channel.sendMessage(msg.build()).flatMap(it -> {
                roleSet.setMessageId(it.getIdLong());

                return RestAction.allOf(roleSet.getRoles()
                        .stream()
                        .map(ReactionRoleBinding::getEmoji)
                        .map(Emoji::fromFormatted)
                        .map(it::addReaction)
                        .toList());
            }).queue();
        }
        guilds.save(prefs);

        return "Reaction messages were resent";
    }

    @Command(permission = "8")
    @Description("Create a set of role reactions")
    public String createset(
            Guild guild, @Command.Arg String name, @Command.Arg String description,
            @Command.Arg TextChannel channel
    ) {
        if (name.contains("-")) throw new CommandError("Name cannot contain dashes (`-`)");

        var              result = guilds.findById(guild.getIdLong());
        var              set    = new ReactionRoleSet(name, description, channel.getIdLong(), new ArrayList<>(), null);
        GuildPreferences prefs;

        if (result.isPresent()) {
            prefs = result.get();

            if (prefs.getRoleSets().stream().map(ReactionRoleSet::getName).anyMatch(name::equals))
                throw new CommandError("A role set with the name `%s` already exists!".formatted(name));

            prefs.getRoleSets().add(set);
        } else prefs = new GuildPreferences(guild.getIdLong(), Set.of(set));

        guilds.save(prefs);
        return "Reaction role set `%s` was created".formatted(name);
    }

    @Command(permission = "8")
    @Description("Remove a set of role reactions")
    public String removeset(
            Guild guild,
            @Command.Arg(autoFillProvider = ReactionRoleSet.AutoFillSetNames.class) String name
    ) {
        var result = guilds.findById(guild.getIdLong());
        if (result.isEmpty()) return "No action performed";

        var prefs = result.get();
        prefs.getRoleSets().removeIf(roleSet -> roleSet.getName().equals(name));
        guilds.save(prefs);
        return "Role reaction set removed";
    }

    @Command(permission = "8")
    @Description("Edit an existing set of role reactions")
    public MessageCreateData editset(
            Guild guild,
            @Command.Arg(autoFillProvider = ReactionRoleSet.AutoFillSetNames.class) String set
    ) {
        final var guildId = guild.getIdLong();
        return guilds.findReactionRoleSet(guildId, set).map(roleSet -> {
            var message = roleSet.createMessage();

            message.addComponents(ActionRow.of(Button.secondary(COMPONENT_ID_ROLESET_EDIT + set, "Edit details..."),
                    Button.secondary(COMPONENT_ID_ROLE_ADD + set, "Add Role..."),
                    Button.danger(COMPONENT_ID_ROLE_REMOVE + set, "Remove Role...")));

            return message.build();
        }).orElseThrow(() -> new CommandError("Could not find reaction role set with name `%s`".formatted(set)));
    }

    @Command(permission = "8")
    @Description("Edit an existing reaction role")
    public MessageCreateData editrole(
            Guild guild, @Command.Arg(autoFillProvider = ReactionRoleSet.AutoFillSetNames.class) String set,
            @Command.Arg(autoFillProvider = ReactionRoleSet.AutoFillRoleNames.class) String role
    ) {
        final var guildId = guild.getIdLong();
        return guilds.findReactionRoleSet(guildId, set).map(roleSet -> {
            var roleBind = roleSet.findBinding(role).orElseThrow(() -> new CommandError("No such role: " + role));
            var embed    = new EmbedBuilder().setColor(new Color(role.hashCode())).addField(roleBind.toField());

            return new MessageCreateBuilder().addEmbeds(embed.build())
                    .addComponents(ActionRow.of(Button.primary(COMPONENT_ID_ROLE_EDIT + set + ':' + role, "Edit...")))
                    .build();
        }).orElseThrow(() -> new CommandError("Could not find reaction role set with name `%s`".formatted(set)));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getGuild() == null || Stream.of(COMPONENT_ID_ROLESET_EDIT,
                COMPONENT_ID_ROLE_ADD,
                COMPONENT_ID_ROLE_REMOVE,
                COMPONENT_ID_ROLE_EDIT).noneMatch(event.getComponentId()::startsWith)) return;

        var    componentId = event.getCustomId();
        var    li          = componentId.lastIndexOf('-');
        var    setName     = componentId.substring(li + 1);
        var    ci          = setName.indexOf(':');
        String roleName    = null;
        if (ci != -1) {
            roleName = setName.substring(ci);
            setName  = setName.substring(0, ci - 1);
        }
        var action  = componentId.substring(0, li + 1);
        var roleSet = guilds.findReactionRoleSet(event.getGuild().getIdLong(), setName).orElseThrow();

        switch (action) {
            case COMPONENT_ID_ROLE_ADD -> event.replyModal(createRoleMutatorModal(componentId,
                    "Select role to add",
                    null).build()).queue();
            case COMPONENT_ID_ROLE_EDIT -> {
                var role = roleSet.findBinding(roleName).orElseThrow();
                event.replyModal(createRoleMutatorModal(componentId, "Editing role", role).build()).queue();
            }
            case COMPONENT_ID_ROLESET_EDIT -> event.replyModal(createRoleSetMutatorModal(componentId,
                    "Editing roleset",
                    roleSet).build()).queue();
            case COMPONENT_ID_ROLE_REMOVE -> {
                var selectMenu = StringSelectMenu.create(OPTION_ID_ROLE);
                for (var role : roleSet.getRoles())
                    selectMenu.addOption(role.getName(),
                            role.getName(),
                            role.getDescription(),
                            Emoji.fromFormatted(role.getEmoji()));
                event.replyModal(Modal.create(componentId, "Select role to remove")
                        .addComponents(Label.of("Role", selectMenu.build()))
                        .build()).queue();
            }
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getGuild() == null || Stream.of(COMPONENT_ID_ROLESET_EDIT,
                COMPONENT_ID_ROLE_ADD,
                COMPONENT_ID_ROLE_REMOVE,
                COMPONENT_ID_ROLE_EDIT).noneMatch(event.getModalId()::startsWith)) return;

        var    componentId = event.getCustomId();
        var    li          = componentId.lastIndexOf('-');
        var    setName     = componentId.substring(li + 1);
        var    ci          = setName.indexOf(':');
        String roleName    = null;
        if (ci != -1) {
            roleName = setName.substring(ci);
            setName  = setName.substring(0, ci - 1);
        }
        var action      = componentId.substring(0, li + 1);
        var prefs       = guilds.findById(event.getGuild().getIdLong()).orElseThrow();
        var roleSet     = prefs.findReactionRoleSet(setName).orElseThrow();
        var interaction = event.getInteraction();

        switch (action) {
            case COMPONENT_ID_ROLESET_EDIT -> {
                var mapping = interaction.getValue(OPTION_ID_CHANNEL);
                if (mapping != null) roleSet.setChannelId(mapping.getAsMentions().getChannels().getFirst().getIdLong());
                mapping = interaction.getValue(OPTION_ID_NAME);
                if (mapping != null && !mapping.getAsString().isBlank()) roleSet.setName(mapping.getAsString());
                mapping = interaction.getValue(OPTION_ID_DESCRIPTION);
                if (mapping != null) roleSet.setDescription(mapping.getAsString());
            }
            case COMPONENT_ID_ROLE_ADD -> {
                var role = Objects.requireNonNull(interaction.getValue(OPTION_ID_ROLE), "role value")
                        .getAsMentions()
                        .getRoles()
                        .getFirst();
                var emoji = Objects.requireNonNull(interaction.getValue(OPTION_ID_EMOJI), "emoji value").getAsString();
                var name  = Objects.requireNonNull(interaction.getValue(OPTION_ID_NAME), "name value").getAsString();
                var description = Objects.requireNonNull(interaction.getValue(OPTION_ID_DESCRIPTION),
                        "description value").getAsString();

                var obj = new ReactionRoleBinding(emoji, name, description, role.getIdLong());
                roleSet.getRoles().add(obj);
            }
            case COMPONENT_ID_ROLE_REMOVE -> {
                var _roleName = Objects.requireNonNull(interaction.getValue(OPTION_ID_ROLE), "role value")
                        .getAsString();
                roleSet.getRoles().removeIf(role -> role.getName().equals(_roleName));
            }
            case COMPONENT_ID_ROLE_EDIT -> {
                var role = roleSet.findBinding(roleName).orElseThrow();

                var mapping = interaction.getValue(OPTION_ID_ROLE);
                if (mapping != null) role.setRoleId(mapping.getAsMentions().getRoles().getFirst().getIdLong());
                mapping = interaction.getValue(OPTION_ID_EMOJI);
                if (mapping != null) role.setEmoji(mapping.getAsString());
                mapping = interaction.getValue(OPTION_ID_NAME);
                if (mapping != null && !mapping.getAsString().isBlank()) role.setName(mapping.getAsString());
                mapping = interaction.getValue(OPTION_ID_DESCRIPTION);
                if (mapping != null) role.setDescription(mapping.getAsString());
            }
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }

        guilds.save(prefs);
        event.reply("This interaction has been successful!").setEphemeral(true).queue();
    }

    @Override
    public void onMessageReactionAdd(@NonNull MessageReactionAddEvent event) {
        reactionInteract(event, event.getGuild()::addRoleToMember);
    }

    @Override
    public void onMessageReactionRemove(@NonNull MessageReactionRemoveEvent event) {
        reactionInteract(event, event.getGuild()::removeRoleFromMember);
    }

    private void reactionInteract(GenericMessageReactionEvent event, BiFunction<User, Role, RestAction<?>> action) {
        var emoji = event.getEmoji();
        var user  = event.getUser();

        if (user == null) return;

        guilds.findReactionRoleSet(event.getGuild().getIdLong(), event.getMessageIdLong())
                .stream()
                .flatMap(roleSet -> roleSet.getRoles()
                        .stream()
                        .filter(role -> role.getEmoji().equals(emoji.getFormatted())))
                .findAny()
                .map(ReactionRoleBinding::getRoleId)
                .map(jda::getRoleById)
                .ifPresent(role -> action.apply(user, role).queue());
    }

    @SuppressWarnings("SameParameterValue")
    private Modal.Builder createRoleSetMutatorModal(String modalId, String title, @Nullable ReactionRoleSet roleSet) {
        var optionChannel     = EntitySelectMenu.create(OPTION_ID_CHANNEL, EntitySelectMenu.SelectTarget.CHANNEL);
        var optionName        = TextInput.create(OPTION_ID_NAME, TextInputStyle.SHORT);
        var optionDescription = TextInput.create(OPTION_ID_DESCRIPTION, TextInputStyle.PARAGRAPH);

        if (roleSet != null) {
            optionChannel.setDefaultValues(EntitySelectMenu.DefaultValue.channel(roleSet.getChannelId()));
            optionName.setRequired(false).setPlaceholder(roleSet.getName());
            optionDescription.setRequired(false).setPlaceholder(roleSet.getDescription());
        }

        return Modal.create(modalId, title)
                .addComponents(Label.of("Channel", optionChannel.build()),
                        Label.of("Name", optionName.build()),
                        Label.of("Description", optionDescription.build()));
    }

    private Modal.Builder createRoleMutatorModal(String modalId, String title, @Nullable ReactionRoleBinding role) {
        var optionRole        = EntitySelectMenu.create(OPTION_ID_ROLE, EntitySelectMenu.SelectTarget.ROLE);
        var optionEmoji       = TextInput.create(OPTION_ID_EMOJI, TextInputStyle.SHORT);
        var optionName        = TextInput.create(OPTION_ID_NAME, TextInputStyle.SHORT);
        var optionDescription = TextInput.create(OPTION_ID_DESCRIPTION, TextInputStyle.PARAGRAPH);

        if (role != null) {
            optionRole.setDefaultValues(EntitySelectMenu.DefaultValue.role(role.getRoleId()));
            optionEmoji.setRequired(false).setPlaceholder(role.getEmoji());
            optionName.setRequired(false).setPlaceholder(role.getName());
            optionDescription.setRequired(false).setPlaceholder(role.getDescription());
        }

        return Modal.create(modalId, title)
                .addComponents(Label.of("Role", optionRole.build()),
                        Label.of("Emoji", optionEmoji.build()),
                        Label.of("Name", optionName.build()),
                        Label.of("Description", optionDescription.build()));
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void on(ApplicationStartedEvent event) {
        event.getApplicationContext().getBean(JDA.class).addEventListener(this);
        event.getApplicationContext().getBean(CommandManager.class).register(this);

        log.info("Initialized");
    }
}
