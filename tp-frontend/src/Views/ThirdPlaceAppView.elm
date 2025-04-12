module Views.ThirdPlaceAppView exposing (view)

import Browser
import ThirdPlaceModel exposing (Model, Msg(..))
import Views.Login.LoginView as LoginView
import Views.Room.RoomModel exposing (ChatMessage, ConversationModel)
import Views.Room.RoomView as RoomView



{-
   Delegates the dom rendering based on whether the user has been authenticated (go to rooms) or not (go to login)
-}


view : Model -> Browser.Document ThirdPlaceModel.Msg
view model =
    { title =
        if model.authenticated then
            "ThirdPlace - Chat"

        else
            "ThirdPlace - Login"
    , body =
        if model.authenticated then
            [ RoomView.view { users = [], conversations = populateDummyConvos, selectedConvo = Nothing } ]

        else
            [ LoginView.view model ]
    }



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
