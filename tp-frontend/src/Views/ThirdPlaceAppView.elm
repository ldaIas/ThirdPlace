module Views.ThirdPlaceAppView exposing (view)

import Browser
import ThirdPlaceModel exposing (Model, Msg(..))
import Views.Login.LoginView as LoginView
import Views.Room.RoomModel
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
            [ RoomView.view model.roomHandler ]

        else
            [ LoginView.view model ]
    }
