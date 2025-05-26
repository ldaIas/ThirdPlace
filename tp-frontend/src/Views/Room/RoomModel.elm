module Views.Room.RoomModel exposing (Model, ConversationModel, ChatMessage, Msg(..), UsrChatMsg(..), ConvosMsg(..), PubsubMsg(..))

import Json.Decode exposing (Value)


{-
    The model for a "room" consisting of several conversations, with each conversation consisting of messages
-}
type alias Model =
    { roomId : String -- The geohash roomId
    , users : List String
    , conversations : List ConversationModel
    , selectedConvo : ConversationModel
    , newConvoDraft : String
    , currentUser : String
    , panelExpansion : Bool -- True for expanding the list of convos panel, false for expanding the chats panel
    }

{-
   The model for each individual conversation in the list of conversations.
-}


type alias ConversationModel =
    { intro : String
    , messages : List ChatMessage
    , participants : List String
    , author : String
    , draftMessage : String
    , convoId : String
    }

type alias ChatMessage = 
    { sender : String
    , content : String
    , convoId : String
    , timestamp : Int
    }

{-
    Msg definition for room events, such as clicking on a conversation or sending a message
-}
type Msg = 
    ConvoClicked ConversationModel
    | ChatPanelMsg UsrChatMsg
    | ConvoPanelMsg ConvosMsg
    | TogglePanels Bool -- Msg for collapsing/expanding the chat and convo panels. True for expanded convos, false for expanded chats
    | RoomPubSubMsg PubsubMsg


{-
    Msg definition for the user chatting
-}
type UsrChatMsg = 
    DraftMessage String
    | SendMessage

type ConvosMsg 
    = UpdateNewConvoDraft String

{-
    Used for publishing and subscribing to messages and conversations
-}
type PubsubMsg
    = SendChatMessage ChatMessage
    | ReceiveChatMessage Value -- comes in as json shaped as RoomModel.ChatMessage
    | SubmitNewConversation
    | ReceiveConversation Value -- comes in as a ConversationModel