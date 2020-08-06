/*
package extractocol.backend;

import java.util.ArrayList;
import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.helper.CFGSerializer;
import extractocol.backend.request.helper.SerEPcontainer;
import extractocol.backend.request.helper.SerializableCFG;
import extractocol.backend.request.helper.CFGSerializer.ICFG_CASE;
import extractocol.backend.response.SignatureBuilder_Response;

public class BackendTester_Response
{

	public static void main(String[] args) throws Exception
	{
		try
		{
			Constants.readDeobfuse(args[1]);
			Constants.readignorelibrary(args[1]);

			int k = 0;
			Constants.serIsForward = true;

			while (k < args.length)
			{
				if (args[k].equalsIgnoreCase("--app"))
				{
					Constants.apkName = args[k + 1];
					k += 2;
				} else if (args[k].equalsIgnoreCase("--backward"))
				{
					Constants.serIsForward = false;
					k++;
				} else if (args[k].equalsIgnoreCase("--heapobject"))
				{
					Constants.heapobject = true;
					k += 2;
				} else if (args[k].equalsIgnoreCase("--debug"))
				{
					Constants.isDebug = true;
					k++;
				} else if (args[k].equalsIgnoreCase("--taintFunctionOnly"))
				{
					Constants.searchMethodFilterUsingTaintFunction = true;
					k++;
				}
			}
			boolean isForward = Constants.serIsForward;

			System.out.println("Response Only Case - 20160602 Hun Namkung");
			System.out.println("App Name : " + Constants.apkName + "\n");
			System.out.println("Deserializing....");
			CFGSerializer CFGs = new CFGSerializer();
			SerializableCFG dsCFG = CFGs.Deserialize(ICFG_CASE.FORWARD);
			List<SerEPcontainer> eplist = CFGs.DeserializeEPC(ICFG_CASE.FORWARD);
			System.out.println("Finished Desrialization");
			System.out.println("\n\n[Serialization Mode] Backend Start! -- " + (isForward ? "Forward" : "Backward"));

			// Start
			Constants.sCFG = dsCFG;

			String SpecificEP = "";
			String DPStmt = "";
			String SpecificDP = "";

			ArrayList<String> allMethods = new ArrayList<String>();

			// tumblr
			if (Constants.apkName.equals("tumblr"))
			{
				// tumblr1
				// SpecificEP =
				// "<com.tumblr.ui.fragment.FollowSomeBlogsFragment:
				// java.util.List parseResponse(org.json.JSONObject,boolean)>";
				// tumblr2
				// SpecificEP = "<com.tumblr.search.SearchHelper:
				// com.tumblr.search.model.OmniSearchResult
				// parseResponse(java.lang.String,com.tumblr.search.model.SearchType,com.tumblr.search.model.SearchQualifier)>";
				// tumblr3
				// SpecificEP =
				// "<com.tumblr.network.response.MentionResponseHandler:
				// java.util.List parseResponse(java.lang.String)>";
				// tumblr4
				// SpecificEP = "<com.tumblr.model.BlogInfo: void
				// fromJson(org.json.JSONObject)>";
				// SpecificEP = "<com.tumblr.model.BlogTheme: void
				// <init>(org.json.JSONObject)>";
				// tumblr5
				// SpecificEP =
				// "<com.buzzfeed.android.activity.BuzzLoginActivity$1$1: void
				// onResponse(java.lang.Object)>";
				// SpecificEP = "<com.tumblr.model.PostAttribution: void
				// <init>(org.json.JSONObject)>";
				// SpecificEP = "<com.tumblr.model.CpiInfo: void
				// <init>(org.json.JSONObject)>";
				// SpecificEP = "<com.tumblr.model.ReblogTrail: void
				// <init>(org.json.JSONArray,long)>";
				// SpecificEP = "<com.tumblr.model.InlineImageInfo: void
				// <init>(org.json.JSONObject)>";

				// tumblr1
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";
				// tumblr2
				// DPStmt = "$r4 = new org.json.JSONObject";
				// tumblr3
				// DPStmt = "$r2 = staticinvoke <com.tumblr.network.ApiUtils:
				// org.json.JSONObject optResponse(java.lang.String)>($r0)";
				// tumblr4
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";
				// tumblr5
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";
				// DPStmt = "$r1 := @parameter0: java.lang.String";
				// DPStmt = "$r1 := @parameter0: org.json.JSONArray";

				// EP 1
				// SpecificEP =
				// "<com.tumblr.social.SocialHelper$LinkTumblrBlogResponseListener:
				// void onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 2
				// SpecificEP =
				// "<com.tumblr.social.SocialHelper$UnlinkTumblrBlogResponseListener:
				// void onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 3
				// SpecificEP = "<com.tumblr.ui.widget.BlogAccountCreateRow$9:
				// void onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 4
				// SpecificEP =
				// "<com.tumblr.messenger.network.BaseMessagingRequest: void
				// onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 5
				SpecificEP = "<com.tumblr.ui.widget.UserBlogOptionsLayout$8$1: void onResponse(org.json.JSONObject)>";
				DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 6
				// SpecificEP =
				// "<com.tumblr.network.retry.processor.SubscriptionRetryTaskProcessor$1:
				// void onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 7
				// SpecificEP = "<com.tumblr.ui.activity.WebViewActivity$2: void
				// onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 8
				// SpecificEP =
				// "<com.tumblr.ui.widget.AbstractBlogOptionsLayout$5: void
				// onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 9
				// SpecificEP =
				// "<com.tumblr.ui.fragment.FollowSomeBlogsFragment$11: void
				// onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				// EP 10
				// SpecificEP =
				// "<com.tumblr.ui.fragment.FollowSomeBlogsFragment$12: void
				// onResponse(org.json.JSONObject)>";
				// DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				SpecificDP = SpecificEP;

			} // pinterest
			else if (Constants.apkName.equals("pinterest"))
			{
				// SpecificEP =
				// "<com.pinterest.activity.board.model.CollaboratorInviteFeed:
				// void
				// <init>(com.pinterest.network.json.PinterestJsonObject,java.lang.String,com.pinterest.api.model.Board,boolean)>";
				// SpecificDP =
				// "<com.pinterest.activity.board.model.CollaboratorInviteFeed:
				// void
				// <init>(com.pinterest.network.json.PinterestJsonObject,java.lang.String,com.pinterest.api.model.Board,boolean)>";
				// DPStmt = "$r1 := @parameter0:
				// com.pinterest.network.json.PinterestJsonObject";
				// allMethods.add("<com.pinterest.activity.board.model.CollaboratorInvite:
				// java.util.List
				// makeAll(com.pinterest.network.json.PinterestJsonArray)>");
				// allMethods.add("<com.pinterest.activity.board.model.CollaboratorInvite:
				// com.pinterest.activity.board.model.CollaboratorInvite
				// make(com.pinterest.network.json.PinterestJsonObject,java.lang.String)>");

				SpecificEP = "<com.pinterest.activity.conversation.ConversationFragment$2: void onSuccess(com.pinterest.api.ApiResponse)>";
				SpecificDP = "<com.pinterest.activity.conversation.ConversationFragment$2: void onSuccess(com.pinterest.api.ApiResponse)>";
				DPStmt = "$r2 = virtualinvoke $r1.<com.pinterest.api.ApiResponse: java.lang.Object getData()>()";

				allMethods.add(
						"<com.pinterest.api.model.Conversation: com.pinterest.api.model.Conversation make(com.pinterest.network.json.PinterestJsonObject)>");
				allMethods.add(
						"<com.pinterest.api.model.Conversation: com.pinterest.api.model.Conversation make(com.pinterest.network.json.PinterestJsonObject,boolean)>");
				allMethods.add(
						"<com.pinterest.api.model.User: com.pinterest.api.model.User make(com.pinterest.network.json.PinterestJsonObject,boolean)>");
				allMethods.add(
						"<com.pinterest.api.model.User: com.pinterest.api.model.User make(com.pinterest.network.json.PinterestJsonObject,boolean,boolean)>");
				allMethods.add(
						"<com.pinterest.api.model.Partner: com.pinterest.api.model.Partner make(com.pinterest.network.json.PinterestJsonObject,boolean)>");

				// Semi Semantic
				allMethods.add("<com.pinterest.network.json.PinterestJsonObject: java.lang.Boolean a(java.lang.String,java.lang.Boolean)>");
				allMethods.add("<com.pinterest.network.json.PinterestJsonObject: java.lang.String a(java.lang.String,java.lang.String)>");
				allMethods.add("<com.pinterest.network.json.PinterestJsonObject: long a(java.lang.String,long)>");
				allMethods
						.add("<com.pinterest.network.json.PinterestJsonObject: com.pinterest.network.json.PinterestJsonObject b(java.lang.String)>");
				allMethods.add("<com.pinterest.network.json.PinterestJsonObject: com.pinterest.network.json.PinterestJsonArray d(java.lang.String)>");
				allMethods.add("<com.pinterest.network.json.PinterestJsonArray: com.pinterest.network.json.PinterestJsonObject d(int)>");
				allMethods.add("<com.pinterest.network.json.PinterestJsonObject: boolean e(java.lang.String)>");
				allMethods.add("<com.pinterest.network.json.PinterestJsonObject: com.pinterest.network.json.PinterestJsonArray d(java.lang.String)>");
			}
			// flipboard
			else if (Constants.apkName.equals("flipboard"))
			{
				// flipboard 1
				SpecificEP = "<flipboard.service.Flap$ReserveUrlRequest: void a()>";
				DPStmt = "$r0 := @parameter0: java.io.InputStream";
				Constants.javaPath = "D:/new_extractocol/tools/dex2jar-2.0/dex2jar-2.0/" + Constants.apkName + "-dex2jar.src/";
				SpecificDP = "<flipboard.json.JsonSerializationWrapper: java.lang.Object a(java.io.InputStream,java.lang.Class)>";

			} else if (Constants.apkName.equals("wish"))
			{
				// 5miles 1
				// SpecificEP = "<com.insthub.fivemiles.a.aq: void
				// callback(java.lang.String,org.json.JSONObject,com.external.androidquery.b.e)>";\
				// SpecificEP = "<com.thirdrock.domain.ItemThumb__JsonHelper: com.thirdrock.domain.ItemThumb parseFromJson(com.fasterxml.jackson.core.JsonParser)>";
				SpecificEP = "<com.contextlogic.wish.api.core.WishApiRequest: void onSuccess(org.json.JSONObject)>";
				SpecificDP = SpecificEP;
				DPStmt = "$r1 := @parameter0: org.json.JSONObject";

				allMethods.add(
						"<com.thirdrock.domain.ItemThumb__JsonHelper: boolean processSingleField(com.thirdrock.domain.ItemThumb,java.lang.String,com.fasterxml.jackson.core.JsonParser)>");
				allMethods.add(
						"<com.thirdrock.domain.User__JsonHelper: com.thirdrock.domain.User parseFromJson(com.fasterxml.jackson.core.JsonParser)>");

				allMethods.add("<com.contextlogic.wish.api.service.GetUserStatusService$1: void onSuccess(com.contextlogic.wish.api.core.WishApiRequest,com.contextlogic.wish.api.core.WishApiResponse)>");
				allMethods.add("<com.contextlogic.wish.api.core.WishApiCallback: void onSuccess(com.contextlogic.wish.api.core.WishApiRequest,com.contextlogic.wish.api.core.WishApiResponse)>");
				allMethods.add(SpecificEP);

				Constants.CurrentEP = SpecificEP; // EP
				Constants.CurrentEntryPoint = SpecificEP;
				Constants.CurrentParentMethod = SpecificDP; // DP
				Constants.TaintFunctions = allMethods; // All Method

				Constants.DPStmt = DPStmt;

				Constants.foundDPStmt = false;
				Constants.methodVisitCount = 0;

				if (Constants.sCFG.getMethodOf(SpecificEP) == null)
				{
					System.out.println("EP not found");
				} else
				{
					System.out.println("found EP");
				}

				SignatureBuilder_Response be = new SignatureBuilder_Response();
				be.StartFingerprint();

			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("error?");
		}
		System.out.println("exit!");
	}

	public static void safePrint(String s)
	{
		synchronized (System.out)
		{
			System.out.print(s);
		}
	}

	public static void safePrintln(String s)
	{
		synchronized (System.out)
		{
			System.out.println(s);
		}
	}
}
*/