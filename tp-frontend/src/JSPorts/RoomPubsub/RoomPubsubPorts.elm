port module JSPorts.RoomPubsub.RoomPubsubPorts exposing (joinRoom, sendMessage, receiveMessage)
import Json.Decode exposing (Value)

port joinRoom : String -> Cmd msg
port sendMessage : Value -> Cmd msg -- Value shaped as either ChatMessage or ConversationModel; decided by JS
port receiveMessage : (Value -> msg) -> Sub msg -- Value shaped as ChatMessage
port receiveConversation : (Value -> msg) -> Sub msg -- Value shaped as ConversationModel