module Views.Room.RoomHandler exposing (..)

import Views.Room.ChatPanel.ChatPanelHandler as ChatPanelHandler
import Views.Room.Conversations.ConversationsHandler as ConversationsHandler
import Views.Room.RoomModel as RoomModel exposing (ChatMessage, ConversationModel, Msg(..))
import JSPorts.Geohash.GeohashHandler as GeohashHandler


init : ( RoomModel.Model, Cmd RoomModel.Msg )
init =
    let
        (initRoomId, geohashCmd) = GeohashHandler.init
    in
    ( { roomId = initRoomId.roomId
      , users = [ "current_user", "meower1", "meower2" ]
      , conversations = populateDummyConvos
      , selectedConvo = initConvo
      , currentUser = "idalas"
      , newConvoDraft = ""
      , panelExpansion = True
      }
    , Cmd.none
    )


initConvo : ConversationModel
initConvo =
    { intro = "Select a conversation to see what others are talking about!"
    , messages = [ { sender = "ThirdPlace", content = "When you select a conversation you'll be able to chat." } ]
    , participants = []
    , author = "ThirdPlace"
    , draftMessage = ""
    }



-- Remove below \/ once we integrate with actual p2p communication


populateDummyConvos : List ConversationModel
populateDummyConvos =
    let
        dummyModel : ConversationModel
        dummyModel =
            { intro = "Meowserrssss", messages = populateDummyMsgs, participants = [ "meower1", "meower2", "current_user" ], author = "meower1", draftMessage = "" }

        dummyModel2 =
            { intro = "rawr xd", messages = populateDummyMsgs2, participants = [ "meower1", "meower2", "current_user" ], author = "meower2", draftMessage = "" }
    in
    [ dummyModel, dummyModel2, dummyModel, dummyModel ]


populateDummyMsgs : List ChatMessage
populateDummyMsgs =
    let
        dummyMsg : ChatMessage
        dummyMsg =
            { sender = "meower1", content = "im spamming :33333333" }

        dummyMsg2 =
            { sender = "current_user", content = "stawp >:L" }
    in
    [ dummyMsg, dummyMsg, dummyMsg, dummyMsg2, dummyMsg, dummyMsg ]


populateDummyMsgs2 : List ChatMessage
populateDummyMsgs2 =
    let
        dummyMsg =
            { sender = "meower2", content = "rawr xd any1 w1t 2 cm 2 my goth show :p" }

        dummyMsg2 =
            { sender = "current_user", content = "no" }
    in
    [ dummyMsg, dummyMsg2, dummyMsg, dummyMsg2 ]


update : RoomModel.Msg -> RoomModel.Model -> ( RoomModel.Model, Cmd RoomModel.Msg )
update msg model =
    case msg of
        ConvoClicked convo ->
            ( { model | selectedConvo = convo }, Cmd.none )

        ChatPanelMsg chatMsg ->
            let
                ( updatedRoomModel, cmd ) =
                    ChatPanelHandler.update chatMsg model
            in
            ( updatedRoomModel, Cmd.map ChatPanelMsg cmd )

        ConvoPanelMsg convoMsg ->
            let
                ( updatedRoomModel, cmd ) =
                    ConversationsHandler.update convoMsg model
            in
            ( updatedRoomModel, cmd )

        TogglePanels expandConvoPanel ->
            ( { model | panelExpansion = expandConvoPanel }, Cmd.none )
