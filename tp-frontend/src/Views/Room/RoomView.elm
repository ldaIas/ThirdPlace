module Views.Room.RoomView exposing (view)

import Html exposing (Html, div, h2, text)
import Html.Attributes exposing (class)
import Views.Room.Conversations.ConversationsView as ConversationsView exposing (view)
import Views.Room.RoomModel exposing (Model, ConversationModel)
import Views.Room.ChatPanel.ChatPanelView as ChatPanelView
import Views.Room.RoomModel as RoomModel
import ThirdPlaceModel


view : Model -> Html ThirdPlaceModel.Msg
view model =
    div [ class "main-container" ]
        [ peopleHere model.users
        , conversations model
        , chatPanel model
        ]


peopleHere : List String -> Html msg
peopleHere users =
    div [ class "container", class "people-here" ]
        (h2 [] [ text "People Here" ] :: List.map userAvatar users)


userAvatar : String -> Html msg
userAvatar name =
    div [ class "user-avatar" ] [ text name ]


conversations : RoomModel.Model -> Html ThirdPlaceModel.Msg
conversations model =
    div [ class "container", class "conversations" ]
        [ ConversationsView.view model ]


chatPanel : RoomModel.Model -> Html ThirdPlaceModel.Msg
chatPanel model =
    ChatPanelView.view model