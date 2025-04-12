module Views.Room.Conversations.ConversationsView exposing (view)

import Html exposing (Html, div, h2, img, p, strong, text)
import Html.Attributes exposing (class, id, src)
import ThirdPlaceModel exposing (Model, Msg)
import Views.Room.Conversations.ConversationStyles exposing (..)
import Views.Room.RoomModel as RoomModel exposing (ConversationModel, Msg(..))
import Html.Events exposing (onClick)
import ThirdPlaceModel exposing (Msg(..))



{-
   The view model for the conversation pane that holsd the conversations going on
-}


view : RoomModel.Model -> Html ThirdPlaceModel.Msg
view model =
    div [ class Views.Room.Conversations.ConversationStyles.convWindow ]
        [ div [ class Views.Room.Conversations.ConversationStyles.convHeader ]
            [ p [ id Views.Room.Conversations.ConversationStyles.convLobbyIdText, class Views.Room.Conversations.ConversationStyles.convHeaderText ] [ text "Lobby ID: 12345" ]
            , div [ class Views.Room.Conversations.ConversationStyles.convHeaderSpacer ] []
            , p [ id Views.Room.Conversations.ConversationStyles.convLobbySizeText, class Views.Room.Conversations.ConversationStyles.convHeaderText ] [ text "Lobby Size: 5/100" ]
            ]
        , div [] [ h2 [] [ text "Conversations" ] ]
        , div [ class Views.Room.Conversations.ConversationStyles.convBody ]
            (List.map (\convo -> conversationBlock convo) model.conversations)
        ]


conversationBlock : ConversationModel -> Html ThirdPlaceModel.Msg
conversationBlock convoModel =
    div [ class Views.Room.Conversations.ConversationStyles.conversationBlock, onClick (RoomMsg (ConvoClicked convoModel)) ]
        [ strong [] [ text (convoModel.author ++ " started a conversation:") ]
        , div [ class Views.Room.Conversations.ConversationStyles.conversationText ] [ text convoModel.intro ]
        , div [ class Views.Room.Conversations.ConversationStyles.convPeopleHere ]
            [ text "People here: "
            , img [ src "sfsef.png" ] []
            , div [ class Views.Room.Conversations.ConversationStyles.convMorePeople ] [ text (String.fromInt (List.length convoModel.participants)) ]
            ]
        ]
