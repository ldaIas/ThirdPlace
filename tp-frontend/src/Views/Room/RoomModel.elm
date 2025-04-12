module Views.Room.RoomModel exposing (Model, ConversationModel, ChatMessage, Msg(..))

type alias Model =
    { users : List String
    , conversations : List ConversationModel
    , selectedConvo : Maybe ConversationModel
    }

{-
   The model for each individual conversation in the list of conversations.
-}


type alias ConversationModel =
    { intro : String
    , messages : List ChatMessage
    , participants : List String
    , author : String
    }

type alias ChatMessage = 
    { sender : String
    , content : String
    }

type Msg = 
    ConvoClicked ConversationModel