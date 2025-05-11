module Views.Room.Conversations.ConversationsView exposing (view)

import Html exposing (Html, div, h2, img, input, p, span, strong, text)
import Html.Attributes exposing (class, id, placeholder, src, value)
import Html.Events exposing (onClick, onInput)
import Json.Decode as Decode
import ThirdPlaceModel exposing (Model, Msg(..))
import Utils.RoomUtils as RoomUtils
import Views.Room.Conversations.ConversationStyles as ConversationStyles exposing (..)
import Views.Room.RoomModel as RoomModel exposing (ConversationModel, ConvosMsg(..), Msg(..))
import Html.Attributes exposing (classList)



{-
   The view model for the conversation pane that holds the conversations going on
-}


view : RoomModel.Model -> Html ThirdPlaceModel.Msg
view model =
    div [ classList [(ConversationStyles.convWindow, True), ("convo-collapsed", (not model.panelExpansion)) ] ]
        [ div [ class ConversationStyles.convHeader ]
            [ p [ id ConversationStyles.convLobbyIdText, class ConversationStyles.convHeaderText ] [ text "Lobby ID: 12345" ]
            , div [ class ConversationStyles.convHeaderSpacer ] []
            , p [ id ConversationStyles.convLobbySizeText, class ConversationStyles.convHeaderText ] [ text "Lobby Size: 5/100" ]
            ]
        , div [] [ h2 [] [ text "Conversations" ] ]
        , div [ class convBody ]
            (List.map (\convo -> conversationBlock convo) model.conversations)
        , newConversationInput model.newConvoDraft
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


newConversationInput : String -> Html ThirdPlaceModel.Msg
newConversationInput draft =
    div [ class ConversationStyles.newConvoBar ]
        [ input
            [ class ConversationStyles.newConvoInput
            , placeholder "Start a new conversation..."
            , value draft
            , onInput (RoomMsg << ConvoPanelMsg << UpdateNewConvoDraft)
            , RoomUtils.setupOnEnter (RoomMsg (ConvoPanelMsg SubmitNewConversation))
            ]
            []
        , span [ class ConversationStyles.newConvoIcon, onClick (RoomMsg (ConvoPanelMsg SubmitNewConversation)) ] [ text "✏️" ]
        ]
