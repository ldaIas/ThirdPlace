module Views.ThirdPlaceAppView exposing (view)

import ThirdPlaceModel exposing (Model, Msg(..))
import Browser
import Views.Room.RoomView as RoomView
import Views.Login.LoginView as LoginView

{-
    -| Delegates the dom rendering based on whether the user has been authenticated (go to rooms) or not (go to login)
-}
view : Model -> Browser.Document Msg
view model =
    { title =
        if model.authenticated then
            "ThirdPlace - Chat"

        else
            "ThirdPlace - Login"
    , body =
        if model.authenticated then
            [ RoomView.view { users = [], conversations = [], messages = [], selectedConvo = Nothing } ]

        else
            [ LoginView.view model ]
    }
