module Views.Room.RoomView exposing (view)

import Html exposing (Html, div, h2, text)
import Html.Attributes exposing (class, classList)
import ThirdPlaceModel
import Views.Room.ChatPanel.ChatPanelView as ChatPanelView
import Views.Room.Conversations.ConversationsView as ConversationsView
import Views.Room.RoomModel as RoomModel exposing (Model)


view : Model -> Html ThirdPlaceModel.Msg
view model =
    div [ classList [ ( "main-container", True ), ( "chat-expanded", not model.panelExpansion ) ] ]
        [ profileAndPeopleSection model
        , conversations model
        , chatPanel model
        ]

profileAndPeopleSection : RoomModel.Model -> Html ThirdPlaceModel.Msg
profileAndPeopleSection model = 
    div [ class "profile-and-people"]
        [ userProfile model, peopleHere model.users]

userProfile : RoomModel.Model -> Html ThirdPlaceModel.Msg
userProfile model =
    div [ class "container", class "user-profile" ]
        [ h2 [] [ text "Profile" ]
        , div [ class "profile-content" ]
            [ div [ class "profile-avatar" ] [ text "U" ]
            , div [ class "profile-info" ]
                [ div [ class "profile-name" ] [ text "current_user" ]
                , div [ class "room-info" ]
                    [ div [ class "room-hash" ] [ text "Room: #abc123" ]
                    , div [ class "room-count" ] [ text ("Users: " ++ String.fromInt (List.length model.users)) ]
                    ]
                ]
            ]
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
