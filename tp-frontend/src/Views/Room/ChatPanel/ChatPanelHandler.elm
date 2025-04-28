module Views.Room.ChatPanel.ChatPanelHandler exposing (update)

{-
   File that handles the update logic for chat panel messages (sending and receiving)
-}

import Views.Room.RoomModel as RoomModel exposing (ChatMessage, Msg(..), UsrChatMsg(..))


update : RoomModel.UsrChatMsg -> RoomModel.Model -> ( RoomModel.Model, Cmd RoomModel.UsrChatMsg )
update chatPanelMsg roomModel =
    let
        oldConvModel : RoomModel.ConversationModel
        oldConvModel =
            roomModel.selectedConvo
    in
    case chatPanelMsg of
        DraftMessage draft ->
            let
                newConvModel : RoomModel.ConversationModel
                newConvModel =
                    { oldConvModel | draftMessage = draft }
            in
            ( { roomModel | selectedConvo = newConvModel }, Cmd.none )

        SendMessage ->
            let
                messageToSend : ChatMessage
                messageToSend =
                    ChatMessage "Me" oldConvModel.draftMessage

                currentConvsList : List RoomModel.ChatMessage
                currentConvsList =
                    oldConvModel.messages

                newConvsList : List RoomModel.ChatMessage
                newConvsList =
                    -- Prevent sending empty string. Also cannot send to ThirdPlace owned chats (system chats)
                    if messageToSend.content /= "" && oldConvModel.author /= "ThirdPlace" then
                        currentConvsList ++ [ messageToSend ]

                    else
                        currentConvsList

                newConvModel : RoomModel.ConversationModel
                newConvModel =
                    { oldConvModel | draftMessage = "", messages = newConvsList }
            in
            ( { roomModel | selectedConvo = newConvModel }, Cmd.none )
