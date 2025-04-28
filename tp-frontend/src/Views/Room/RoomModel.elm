module Views.Room.RoomModel exposing (Model, ConversationModel, ChatMessage, Msg(..), UsrChatMsg(..))

{-
    The model for a "room" consisting of several conversations, with each conversation consisting of messages
-}
type alias Model =
    { users : List String
    , conversations : List ConversationModel
    , selectedConvo : ConversationModel
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
    }

type alias ChatMessage = 
    { sender : String
    , content : String
    }

{-
    Msg definition for room events, such as clicking on a conversation or sending a message
-}
type Msg = 
    ConvoClicked ConversationModel
    | ChatPanelMsg UsrChatMsg


{-
    Msg definition for the user chatting
-}
type UsrChatMsg = 
    DraftMessage String
    | SendMessage