package ru.bgcrm.plugin.slack;

import com.fasterxml.jackson.databind.JsonNode;

import ru.bgcrm.plugin.slack.dao.SlackProto;

/**
 * 1. Создать команду (bgcrm), аккаунт (bgcrm)
 * 2. Создать приложение.
 * Client ID 		112244252083.111636011841
 * Client Secret		a8f7c329182524aaf5559d0611000e61
 * 3. OAuth Settings
 * You must specify at least one redirect URL for OAuth to work. If you pass a URL in an OAuth request, it must (at least partially) match one of the URLs you enter here. Learn more
 * Redirect URL(s)
 * 4. https://slack.com/oauth/authorize?client_id=112244252083.111636011841&scope=channels:history channels:read channels:write users:read groups:read groups:write groups:history chat:write:user chat:write:bot
 * -> http://bgcrm.ru/oauth?code=112244252083.130808036180.f7a36c46e4
 * 5. https://slack.com/api/oauth.access?code=112244252083.130808036180.f7a36c46e4&client_id=112244252083.111636011841&client_secret=a8f7c329182524aaf5559d0611000e61  
 * -> {"ok":true,"access_token":"xoxp-112244252083-112987389558-112322797394-6aa3f151be1725ee6ee0265a2dff9b5e","scope":"identify,channels:history,groups:history,channels:read,groups:read,users:read,users:read.email,channels:write,chat:write:user,chat:write:bot,groups:write","user_id":"U3AV1BFGE","team_name":"bgcrm","team_id":"T3A767E2F"}
 */
public class Test {
	
	public static void main(String[] args) {
		try {
			SlackProto proto = new SlackProto("xoxp-112244252083-112987389558-112322797394-6aa3f151be1725ee6ee0265a2dff9b5e", true);
			//proto.createChannel("test");
			
			//{"ok":true,"channel":{"id":"C39J4BREU","name":"test","is_channel":true,"created":1480802565,"creator":"U3AV1BFGE","is_archived":false,"is_general":false,"is_member":true,"last_read":"0000000000.000000","latest":null,"unread_count":0,"unread_count_display":0,"members":["U3AV1BFGE"],"topic":{"value":"","creator":"","last_set":0},"purpose":{"value":"","creator":"","last_set":0}},"original_team":{"id":"112244252083","db_shard":"601","ms_shard":"831","ds_shard":"37","redis_shard":"0","solr_shard":"742","solr_shard_2":"0","solr_files_shard":"160","solr_files_shard_2":"0","solr_users_shard":"1","solr_users_shard_2":"0","db_shard_lock":"0","ms_shard_lock":"0","ds_shard_lock":"0","redis_shard_lock":"0","solr_shard_lock":"0","name":"bgcrm","date_create":"1480796759","date_archive":"0","date_delete":"0","admin_user_id":"112987389558","domain":"bgcrm","email_domain":"bgcrm.ru","prefs":"{\"invites_only_admins\":false,\"invites_limit\":true,\"default_channels\":[\"111635770353\",\"111635770385\"]}","invite_batch":"signup_api","invite_bucket":"","credits":"0","pay_prod_cur":"","pay_prod_next":"","pay_date_next":"0000-00-00","pay_users":"0","limit_ts":"0","is_idle":"0","idle_batch":"","referring_team_id":"0","referring_code_id":"0","refer_has_paid":"0","enterprise_id":"0","is_enterprise":"0","date_freed":"0","was_in_preview":false,"enterprise":[],"was_prepped":true}}

			//proto.setChannelTopic("C39J4BREU", "Иванов иван иванович не работает интернет ленина 6");
			
			//proto.setChannelPurpose("C39J4BREU", "тестовое назначение канала");
			
			//proto.userList();
			//{"ok":true,"members":[{"id":"U3AV1BFGE","team_id":"T3A767E2F","name":"bgcrm","deleted":false,"status":null,"color":"9f69e7","real_name":"Test Test","tz":"Europe\/Amsterdam","tz_label":"Central European Time","tz_offset":3600,"profile":{"first_name":"Test","last_name":"Test","avatar_hash":"g5eab904409e","real_name":"Test Test","real_name_normalized":"Test Test","email":"info@bgcrm.ru","image_24":"https:\/\/secure.gravatar.com\/avatar\/5eab904409e8736b246c350b486b976f.jpg?s=24&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0002-24.png","image_32":"https:\/\/secure.gravatar.com\/avatar\/5eab904409e8736b246c350b486b976f.jpg?s=32&d=https%3A%2F%2Fa.slack-edge.com%2F0180%2Fimg%2Favatars%2Fava_0002-32.png","image_48":"https:\/\/secure.gravatar.com\/avatar\/5eab904409e8736b246c350b486b976f.jpg?s=48&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0002-48.png","image_72":"https:\/\/secure.gravatar.com\/avatar\/5eab904409e8736b246c350b486b976f.jpg?s=72&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0002-72.png","image_192":"https:\/\/secure.gravatar.com\/avatar\/5eab904409e8736b246c350b486b976f.jpg?s=192&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0002-192.png","image_512":"https:\/\/secure.gravatar.com\/avatar\/5eab904409e8736b246c350b486b976f.jpg?s=512&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0002-512.png"},"is_admin":true,"is_owner":true,"is_primary_owner":true,"is_restricted":false,"is_ultra_restricted":false,"is_bot":false,"has_2fa":false},{"id":"U3ADN2ZHB","team_id":"T3A767E2F","name":"vova","deleted":false,"status":null,"color":"4bbe2e","real_name":"Vova Test","tz":"Europe\/Amsterdam","tz_label":"Central European Time","tz_offset":3600,"profile":{"first_name":"Vova","last_name":"Test","avatar_hash":"g0d85f8d81c1","real_name":"Vova Test","real_name_normalized":"Vova Test","email":"vova@mailinator.com","image_24":"https:\/\/secure.gravatar.com\/avatar\/0d85f8d81c1a5e0d8daf7ef9a0afc07f.jpg?s=24&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-24.png","image_32":"https:\/\/secure.gravatar.com\/avatar\/0d85f8d81c1a5e0d8daf7ef9a0afc07f.jpg?s=32&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-32.png","image_48":"https:\/\/secure.gravatar.com\/avatar\/0d85f8d81c1a5e0d8daf7ef9a0afc07f.jpg?s=48&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-48.png","image_72":"https:\/\/secure.gravatar.com\/avatar\/0d85f8d81c1a5e0d8daf7ef9a0afc07f.jpg?s=72&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-72.png","image_192":"https:\/\/secure.gravatar.com\/avatar\/0d85f8d81c1a5e0d8daf7ef9a0afc07f.jpg?s=192&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0015-192.png","image_512":"https:\/\/secure.gravatar.com\/avatar\/0d85f8d81c1a5e0d8daf7ef9a0afc07f.jpg?s=512&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0015-512.png"},"is_admin":false,"is_owner":false,"is_primary_owner":false,"is_restricted":false,"is_ultra_restricted":false,"is_bot":false,"has_2fa":false},{"id":"USLACKBOT","team_id":"T3A767E2F","name":"slackbot","deleted":false,"status":null,"color":"757575","real_name":"slackbot","tz":null,"tz_label":"Pacific Standard Time","tz_offset":-28800,"profile":{"first_name":"slackbot","last_name":"","image_24":"https:\/\/a.slack-edge.com\/0180\/img\/slackbot_24.png","image_32":"https:\/\/a.slack-edge.com\/2fac\/plugins\/slackbot\/assets\/service_32.png","image_48":"https:\/\/a.slack-edge.com\/2fac\/plugins\/slackbot\/assets\/service_48.png","image_72":"https:\/\/a.slack-edge.com\/0180\/img\/slackbot_72.png","image_192":"https:\/\/a.slack-edge.com\/66f9\/img\/slackbot_192.png","image_512":"https:\/\/a.slack-edge.com\/1801\/img\/slackbot_512.png","avatar_hash":"sv1444671949","real_name":"slackbot","real_name_normalized":"slackbot","fields":null},"is_admin":false,"is_owner":false,"is_primary_owner":false,"is_restricted":false,"is_ultra_restricted":false,"is_bot":false}],"cache_ts":1480845413}

			//proto.channelInviteUsers("C39J4BREU", Arrays.asList("U3ADN2ZHB"));
			//"vova@bgcrm.ru", 
			
			//proto.chatPostMessage("G3GJ3TW9X", "Тестовое сообщение от BGCRM.", null, null);
			
			//proto.channelHistory("C39J4BREU", null);
			//proto.channelHistory("C39J4BREU", "1481482200");
			//{"ok":true,"messages":[{"type":"message","user":"U3AV1BFGE","text":"\u0422\u0435\u0441\u0442\u043e\u0432\u043e\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u043e\u0442 BGCRM.","bot_id":"B39K0S9QR","ts":"1480846181.000004"},{"user":"U3ADN2ZHB","inviter":"U3AV1BFGE","text":"<@U3ADN2ZHB|vova> has joined the channel","type":"message","subtype":"channel_join","ts":"1480845613.000003"},{"user":"U3AV1BFGE","purpose":"\u0442\u0435\u0441\u0442\u043e\u0432\u043e\u0435 \u043d\u0430\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u043a\u0430\u043d\u0430\u043b\u0430","text":"<@U3AV1BFGE|bgcrm> set the channel purpose: \u0442\u0435\u0441\u0442\u043e\u0432\u043e\u0435 \u043d\u0430\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u043a\u0430\u043d\u0430\u043b\u0430","type":"message","subtype":"channel_purpose","ts":"1480841960.000002"},{"user":"U3AV1BFGE","topic":"\u0418\u0432\u0430\u043d\u043e\u0432 \u0438\u0432\u0430\u043d \u0438\u0432\u0430\u043d\u043e\u0432\u0438\u0447 \u043d\u0435 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442 \u043b\u0435\u043d\u0438\u043d\u0430 6","text":"<@U3AV1BFGE|bgcrm> set the channel topic: \u0418\u0432\u0430\u043d\u043e\u0432 \u0438\u0432\u0430\u043d \u0438\u0432\u0430\u043d\u043e\u0432\u0438\u0447 \u043d\u0435 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442 \u043b\u0435\u043d\u0438\u043d\u0430 6","type":"message","subtype":"channel_topic","ts":"1480803022.000003"},{"user":"U3AV1BFGE","text":"<@U3AV1BFGE|bgcrm> has joined the channel","type":"message","subtype":"channel_join","ts":"1480802565.000002"}],"has_more":false}
			
			/*JsonNode node = proto.channelList(true);
			for (JsonNode channel : proto.channelList(true).get("groups")) {
				System.out.println(channel.get("id") + " " + channel.get("name"));
			}*/
			//Response: {"ok":true,"channels":[{"id":"C39JPNNAD","name":"general","is_channel":true,"created":1480796760,"creator":"U3AV1BFGE","is_archived":false,"is_general":true,"is_member":true,"members":["U3ADN2ZHB","U3AV1BFGE"],"topic":{"value":"Company-wide announcements and work-based matters","creator":"","last_set":0},"purpose":{"value":"This channel is for team-wide communication and announcements. All team members are in this channel.","creator":"","last_set":0},"num_members":2},{"id":"C39JPNNBB","name":"random","is_channel":true,"created":1480796760,"creator":"U3AV1BFGE","is_archived":false,"is_general":false,"is_member":true,"members":["U3ADN2ZHB","U3AV1BFGE"],"topic":{"value":"Non-work banter and water cooler conversation","creator":"","last_set":0},"purpose":{"value":"A place for non-work-related flimflam, faffing, hodge-podge or jibber-jabber you'd prefer to keep out of more focused work-related channels.","creator":"","last_set":0},"num_members":2},{"id":"C39J4BREU","name":"test","is_channel":true,"created":1480802565,"creator":"U3AV1BFGE","is_archived":false,"is_general":false,"is_member":true,"members":["U3ADN2ZHB","U3AV1BFGE"],"topic":{"value":"\u0418\u0432\u0430\u043d\u043e\u0432 \u0438\u0432\u0430\u043d \u0438\u0432\u0430\u043d\u043e\u0432\u0438\u0447 \u043d\u0435 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442 \u043b\u0435\u043d\u0438\u043d\u0430 6","creator":"U3AV1BFGE","last_set":1480803022},"purpose":{"value":"\u0442\u0435\u0441\u0442\u043e\u0432\u043e\u0435 \u043d\u0430\u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435 \u043a\u0430\u043d\u0430\u043b\u0430","creator":"U3AV1BFGE","last_set":1480841960},"num_members":2}]}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
