module Views.Room.RoomModel exposing (Model, ConversationModel)

type alias Model =
    { users : List String
    , conversations : List ConversationModel
    , messages : List String
    , selectedConvo : Maybe ConversationModel
    }

{-
   -| The model for each individual conversation in the list of conversations.
-}


type alias ConversationModel =
    { intro : String
    , messages : List String
    , participants : List String
    , author : String
    }