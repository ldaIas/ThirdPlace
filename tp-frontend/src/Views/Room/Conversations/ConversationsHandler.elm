module Views.Room.Conversations.ConversationsHandler exposing (update)
import Views.Room.RoomModel as RoomModel exposing (ConvosMsg(..), Model, ConversationModel, Msg(..))

update : ConvosMsg -> Model -> (Model, Cmd RoomModel.Msg)
update msg model = 
    case msg of 
        UpdateNewConvoDraft newText ->
            ( { model | newConvoDraft = newText }, Cmd.none)

        SubmitNewConversation ->
            if String.trim model.newConvoDraft /= "" then
                let
                    newConvo : ConversationModel
                    newConvo = 
                        { author = model.currentUser 
                        , intro = model.newConvoDraft
                        , messages = []
                        , participants = [model.currentUser]
                        , draftMessage = ""
                        }
                in
                ( { model | conversations = newConvo :: model.conversations, newConvoDraft = ""}, Cmd.none)

            else
                ( model, Cmd.none )