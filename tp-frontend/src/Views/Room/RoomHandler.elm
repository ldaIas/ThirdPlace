module Views.Room.RoomHandler exposing (..)
import Views.Room.RoomModel as RoomModel exposing (ConversationModel, ChatMessage)
import Views.Room.RoomModel exposing (Msg(..))

init : (RoomModel.Model, Cmd RoomModel.Msg)
init = 
    ( {users = ["current_user", "meower1", "meower2"], conversations = populateDummyConvos, selectedConvo = Nothing}, Cmd.none)

-- Remove once we integrate with actual p2p communication


populateDummyConvos : List ConversationModel
populateDummyConvos =
    let
        dummyModel =
            { intro = "Meowserrssss", messages = populateDummyMsgs, participants = [ "meower1", "meower2", "current_user" ], author = "meower1" }

        dummyModel2 =
            { intro = "rawr xd", messages = populateDummyMsgs2, participants = [ "meower1", "meower2", "current_user" ], author = "meower2" }
    in
    [ dummyModel, dummyModel2, dummyModel, dummyModel ]


populateDummyMsgs : List ChatMessage
populateDummyMsgs =
    let
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

update : RoomModel.Msg -> RoomModel.Model -> (RoomModel.Model, Cmd RoomModel.Msg)
update msg model =
    case msg of
        ConvoClicked convo ->
            ( {model | selectedConvo = Just convo}, Cmd.none)