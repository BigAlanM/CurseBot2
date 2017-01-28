package me.urielsalis.cursebot.extensions.ModCommands;

import me.urielsalis.cursebot.api.*;
import me.urielsalis.cursebot.events.CommandEvent;
import me.urielsalis.cursebot.events.MessageEvent;
import me.urielsalis.cursebot.extensions.Extension;
import me.urielsalis.cursebot.extensions.ExtensionApi;
import me.urielsalis.cursebot.extensions.ExtensionHandler;
import net.engio.mbassy.listener.Handler;

import java.util.*;

/**
 * Created by urielsalis on 1/28/2017
 */
@Extension(name = "ModCommands", version = "1.0.0", id = "Modcommands/1.0.0")
public class Main {
    ExtensionApi extApi;
    CurseApi api;
    private HashMap<Long, Long> banned = new HashMap<>();

    @ExtensionHandler.ExtensionInit("Profanity/1.0.0")
    public void init(ExtensionApi api2) {
        this.extApi = api2;
        extApi.addListener("commands", new ModCommandsListener());
        Timer timer = new Timer();
        TimerTask unban = new TimerTask() {
            @Override
            public void run() {
                updateBans(System.currentTimeMillis() / 1000L);
            }
        };

        timer.schedule(unban,0l,1000*60);
        api = extApi.getCurseAPI();
    }

    private class ModCommandsListener implements ExtensionApi.Listener {
        @Override
        public String name() {
            return "ModCommandsListener/1.0.0";
        }

        @Handler
        public void handle(ExtensionApi.Event event) {
            if(event instanceof CommandEvent) {
                CommandEvent commandEvent = (CommandEvent) event;
                handleCommand(commandEvent.command);
            }
        }
    }

    private void handleCommand(Command command) {
        switch (command.command) {
            case "delete":
            {
                if(!Util.isUserAuthorized(command.message.senderName)) return;
                api.postMessage(api.resolveChannel("bot-log"), "Executing delete command! sender: " + command.message.senderName + " Deleted messages:");
                String channelName = command.args[0];
                String username = command.args[1];
                int count = Integer.parseInt(command.args[2]);
                int counter = 0;
                Channel channel = api.resolveChannel(channelName);
                for(Message message1 : channel.messages) {
                    if(counter > count) break;
                    if(Util.equals(username, message1.senderName)) {
                        if(message1.isPM) {
                            api.postMessage(api.resolveChannel("bot-log"), Util.timestampToDate(message1.timestamp) + "  [" + message1.senderName + "] " + message1.body);
                        } else {
                            api.postMessage(api.resolveChannel("bot-log"), Util.timestampToDate(message1.timestamp) + "  <" + message1.senderName + "> " + message1.body);
                        }
                        api.deleteMessage(message1);
                                        /*try {
                                            TimeUnit.SECONDS.sleep(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }*/
                        counter++;
                    }
                }
            }
            break;
            case "kick":
            {
                if(!Util.isUserAuthorized(command.message.senderName)) return;
                String username = command.args[0];
                if(!Util.isUserAuthorized(username)) {
                    api.kickUser(api.resolveMember(username));
                    api.postMessage(api.resolveChannel("bot-log"), "Kicked user "+username + ". Sender: "+command.message.senderName);
                }
            }
            break;
            case "ban":
            {
                if(!Util.isUserAuthorized(command.message.senderName)) return;
                String username = command.args[0];
                int minutes = Integer.parseInt(command.args[1]);
                String reason = "";

                if(command.args.length > 2)
                    reason = Util.spaceSeparatedString(Arrays.copyOfRange(command.args, 2, command.args.length)).replaceAll("/n", "\n");
                Member member = api.resolveMember(username);
                if(member!=null) {
                    api.banMember(member.senderID, reason);
                    banned.put(member.senderID, command.message.timestamp+(minutes*60));
                    api.postMessage(api.resolveChannel("bot-log"), "Banned user "+username + ". Sender: "+command.message.senderName + " Reason: " + reason + " Minutes: " + minutes);
                }
            }
            break;
            case "quit":
            {
                if(!Util.isUserAuthorized(command.message.senderName)) return;
                api.postMessage(api.resolveChannel("bot-log"), "Shut down command executed! sender: " + command.message.senderName);
                System.exit(0);
            }
            break;
        }
    }

    private void updateBans(long timestamp) {
        ArrayList<Long> toUnban = new ArrayList<>();
        for(Map.Entry<Long, Long> entry: banned.entrySet()) {
            if(entry.getValue() < timestamp) {
                toUnban.add(entry.getKey());
            }
        }
        for(long id: toUnban) {
            banned.remove(id);
            api.unBanMember(id);
        }
    }
}
