module Views.Room.Conversations.ConversationsHandler exposing (update)
import Views.Room.RoomModel as RoomModel exposing (ConvosMsg(..), Model, Msg(..))

update : ConvosMsg -> Model -> (Model, Cmd RoomModel.Msg)
update msg model = 
    case msg of 
        UpdateNewConvoDraft newText ->
            ( { model | newConvoDraft = newText }, Cmd.none)
            