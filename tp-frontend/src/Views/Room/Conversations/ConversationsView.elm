module Views.Room.Conversations.ConversationsView exposing (view)

import Html exposing (Html, div, h2, img, p, strong, text)
import Html.Attributes exposing (class, id, src)
import ThirdPlaceModel exposing (Model, Msg)
import Views.Room.Conversations.ConversationStyles exposing (..)
import Views.Room.RoomModel as RoomModel exposing (ConversationModel, Msg(..))
import Html.Events exposing (onClick)
import ThirdPlaceModel exposing (Msg(..))
import Views.Room.Conversations.ConversationStyles as ConversationStyles



{-
   The view model for the conversation pane that holds the conversations going on
-}


view : RoomModel.Model -> Html ThirdPlaceModel.Msg
view model =
    div [ class ConversationStyles.convWindow ]
        [ div [ class ConversationStyles.convHeader ]
            [ p [ id ConversationStyles.convLobbyIdText, class ConversationStyles.convHeaderText ] [ text "Lobby ID: 12345" ]
            , div [ class ConversationStyles.convHeaderSpacer ] []
            , p [ id ConversationStyles.convLobbySizeText, class ConversationStyles.convHeaderText ] [ text "Lobby Size: 5/100" ]
            ]
        , div [] [ h2 [] [ text "Conversations" ] ]
        , div [ class convBody ]
            (List.map (\convo -> conversationBlock convo) model.conversations)
        ]


conversationBlock : ConversationModel -> Html ThirdPlaceModel.Msg
conversationBlock convoModel =
    div [ class ConversationStyles.conversationBlock, onClick (RoomMsg (ConvoClicked convoModel)) ]
        [ strong [] [ text (convoModel.author ++ " started a conversation:") ]
        , div [ class ConversationStyles.conversationText ] [ text convoModel.intro ]
        , div [ class ConversationStyles.convPeopleHere ]
            [ text "People here: "
            , img [ src "sfsef.png" ] []
            , div [ class ConversationStyles.convMorePeople ] [ text (String.fromInt (List.length convoModel.participants)) ]
            ]
        ]
