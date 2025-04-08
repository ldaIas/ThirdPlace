module Views.Room.Conversations.ConversationsView exposing (view)

import Views.Room.Conversations.ConversationStyles exposing (..)
import Html exposing (Html, div, h2, img, p, strong, text)
import Html.Attributes exposing (class, id, src)
import ThirdPlaceModel exposing (Model, Msg)
import Views.Room.RoomModel exposing (ConversationModel)


view : List ConversationModel -> Html msg
view _ =
    div [ class Views.Room.Conversations.ConversationStyles.convWindow ]
        [ div [ class Views.Room.Conversations.ConversationStyles.convHeader ]
            [ p [ id Views.Room.Conversations.ConversationStyles.convLobbyIdText, class Views.Room.Conversations.ConversationStyles.convHeaderText ] [ text "Lobby ID: 12345" ]
            , div [ class Views.Room.Conversations.ConversationStyles.convHeaderSpacer ] []
            , p [ id Views.Room.Conversations.ConversationStyles.convLobbySizeText, class Views.Room.Conversations.ConversationStyles.convHeaderText ] [ text "Lobby Size: 5/100" ]
            ]
        , div [] [ h2 [] [ text "Conversations" ] ]
        , div [ class Views.Room.Conversations.ConversationStyles.convBody ]
            [ conversationBlock
            , conversationBlock
            , conversationBlock
            , conversationBlock
            , conversationBlock
            , conversationBlock
            , conversationBlock
            , conversationBlock
            ]
        ]


conversationBlock : Html msg
conversationBlock =
    div [ class Views.Room.Conversations.ConversationStyles.conversationBlock ]
        [ strong [] [ text "Alicia started a conversation:" ]
        , div [ class Views.Room.Conversations.ConversationStyles.conversationText ] [ text "Hey everyone, first time here. What are y'all up to today?" ]
        , div [ class Views.Room.Conversations.ConversationStyles.convPeopleHere ]
            ([ text "People here: " ]
                ++ [ img [ src "sfsef.png" ] [] ]
                ++ [ div [ class Views.Room.Conversations.ConversationStyles.convMorePeople ] [ text "+3" ] ]
            )
        ]
