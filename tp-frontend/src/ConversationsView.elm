module ConversationsView exposing (ConversationModel, view)

import ConversationStyles exposing (..)
import Html exposing (Html, div, h2, img, p, strong, text)
import Html.Attributes exposing (class, id, src)
import ThirdPlaceModel exposing (Model, Msg)



{-
   -| The model for each individual conversation in the list of conversations.
-}


type alias ConversationModel =
    { intro : String
    , messages : List String
    , participants : List String
    , author : String
    }


view : List ConversationModel -> Html msg
view _ =
    div [ class ConversationStyles.convWindow ]
        [ div [ class ConversationStyles.convHeader ]
            [ p [ id ConversationStyles.convLobbyIdText, class ConversationStyles.convHeaderText ] [ text "Lobby ID: 12345" ]
            , div [ class ConversationStyles.convHeaderSpacer ] []
            , p [ id ConversationStyles.convLobbySizeText, class ConversationStyles.convHeaderText ] [ text "Lobby Size: 5/100" ]
            ]
        , div [] [ h2 [] [ text "Conversations" ] ]
        , div [ class ConversationStyles.convBody ]
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
    div [ class ConversationStyles.conversationBlock ]
        [ strong [] [ text "Alicia started a conversation:" ]
        , div [ class ConversationStyles.conversationText ] [ text "Hey everyone, first time here. What are y'all up to today?" ]
        , div [ class ConversationStyles.convPeopleHere ]
            ([ text "People here: " ]
                ++ [ img [ src "sfsef.png" ] [] ]
                ++ [ div [ class ConversationStyles.convMorePeople ] [ text "+3" ] ]
            )
        ]
