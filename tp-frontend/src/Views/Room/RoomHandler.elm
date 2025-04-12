module Views.Room.RoomHandler exposing (..)
import Views.Room.RoomModel as RoomModel
import Views.Room.RoomModel exposing (Msg(..))

init : (RoomModel.Model, Cmd RoomModel.Msg)
init = 
    ( {users = [], conversations = [], selectedConvo = Nothing}, Cmd.none)

update : RoomModel.Msg -> RoomModel.Model -> (RoomModel.Model, Cmd RoomModel.Msg)
update msg model =
    case msg of
        ConvoClicked convo ->
            ( {model | selectedConvo = Just convo}, Cmd.none)