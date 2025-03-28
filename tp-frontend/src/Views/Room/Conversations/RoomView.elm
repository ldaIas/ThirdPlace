module Views.Room.Conversations.RoomView exposing (view)

import Views.Room.Conversations.ConversationsView exposing (ConversationModel, view)
import Html exposing (Html, div, h2, text)
import Html.Attributes exposing (class)


type alias ChatRoomModel =
    { users : List String
    , conversations : List ConversationModel
    , messages : List String
    }


view : ChatRoomModel -> Html msg
view model =
    div [ class "main-container" ]
        [ peopleHere model.users
        , conversations model.conversations
        , chatPanel model.messages
        ]


peopleHere : List String -> Html msg
peopleHere users =
    div [ class "container", class "people-here" ]
        (h2 [] [ text "People Here" ] :: List.map userAvatar users)


userAvatar : String -> Html msg
userAvatar name =
    div [ class "user-avatar" ] [ text name ]


conversations : List ConversationModel -> Html msg
conversations convos =
    div [ class "container", class "conversations" ]
        [ Views.Room.Conversations.ConversationsView.view convos ]


chatPanel : List String -> Html msg
chatPanel messages =
    div [ class "container", class "chat-panel" ]
        (h2 [] [ text "Conversation" ] :: List.map chatMessage messages)


chatMessage : String -> Html msg
chatMessage msg =
    div [ class "message" ] [ text msg ]
