module JSPorts.RoomPubsub.RoomPubsubHandler exposing (update)

import JSPorts.RoomPubsub.RoomPubsubPorts exposing (sendMessage)
import Json.Decode as Decode exposing (Value)
import Json.Encode as Encode
import List exposing (head)
import Platform.Cmd as Cmd
import Views.Room.RoomModel as RoomModel exposing (ChatMessage, ConversationModel, PubsubMsg(..), UsrChatMsg(..))



{-
   When we call update we will create a new message based on if we sent it or received it
-}


update : RoomModel.Model -> PubsubMsg -> ( RoomModel.Model, Cmd msg )
update model msg =
    case msg of
        SendChatMessage message ->
            let
                sendChatCmd =
                    -- Serialize the ChatMessage into JSON
                    sendMessage <| encodeChatMessage message
            in
            ( model, sendChatCmd )

        ReceiveChatMessage messageValue ->
            case Decode.decodeValue decodeChatMessage messageValue of
                Ok inMsg ->
                    -- Add message to appropriate conversation based on convoId
                    let
                        affectedConvo : ConversationModel
                        affectedConvo =
                            head (List.filter (\c -> c.convoId == inMsg.convoId) model.conversations)
                                |> Maybe.withDefault
                                    { intro = "error conversation. report bug if seen"
                                    , messages = []
                                    , participants = []
                                    , author = "n/a"
                                    , draftMessage = ""
                                    , convoId = "n/a"
                                    }

                        updatedChats : List ChatMessage
                        updatedChats =
                            affectedConvo.messages ++ [ inMsg ]

                        updatedConvos : List ConversationModel
                        updatedConvos =
                            List.map
                                (\c ->
                                    if c.convoId == inMsg.convoId then
                                        { c | messages = updatedChats }

                                    else
                                        c
                                )
                                model.conversations
                    in
                    ( { model | conversations = updatedConvos }, Cmd.none )

                Err err ->
                    let
                        _ =
                            Debug.log "error received" Decode.errorToString err
                    in
                    ( model, Cmd.none )

        SubmitNewConversation ->
            if String.trim model.newConvoDraft /= "" then
                let
                    newConvo : ConversationModel
                    newConvo =
                        { author = model.currentUser
                        , intro = model.newConvoDraft
                        , messages = []
                        , participants = [ model.currentUser ]
                        , draftMessage = ""
                        , convoId = "test-id"
                        }

                    newConvoJson =
                        encodeConvoMessage newConvo

                    submitNewConvoMsg =
                        sendMessage newConvoJson
                in
                ( { model | newConvoDraft = "" }, submitNewConvoMsg )

            else
                ( model, Cmd.none )

        ReceiveConversation convoValue ->
            case Decode.decodeValue decodeConvoMessage convoValue of
                Ok inConvo ->
                    -- Add new convo to list
                    let
                        newConvosList : List ConversationModel
                        newConvosList =
                            model.conversations ++ [ inConvo ]
                    in
                    ( { model | conversations = newConvosList }, Cmd.none )

                Err err ->
                    let
                        _ =
                            Debug.log "error received decoding conversation model" Decode.errorToString err
                    in
                    ( model, Cmd.none )


decodeChatMessage : Decode.Decoder RoomModel.ChatMessage
decodeChatMessage =
    Decode.map4 RoomModel.ChatMessage
        (Decode.field "convoId" Decode.string)
        (Decode.field "sender" Decode.string)
        (Decode.field "content" Decode.string)
        (Decode.field "timestamp" Decode.int)


decodeConvoMessage : Decode.Decoder RoomModel.ConversationModel
decodeConvoMessage =
    Decode.map6 RoomModel.ConversationModel
        (Decode.field "intro" Decode.string)
        (Decode.field "messages" (Decode.list decodeChatMessage))
        (Decode.field "participants" (Decode.list Decode.string))
        (Decode.field "author" Decode.string)
        (Decode.field "draftMessage" Decode.string)
        (Decode.field "convoId" Decode.string)


encodeChatMessage : RoomModel.ChatMessage -> Value
encodeChatMessage chatMsg =
    Encode.object
        [ ( "convoId", Encode.string chatMsg.convoId )
        , ( "sender", Encode.string chatMsg.sender )
        , ( "content", Encode.string chatMsg.content )
        , ( "timestamp", Encode.int chatMsg.timestamp )
        ]


encodeConvoMessage : RoomModel.ConversationModel -> Value
encodeConvoMessage convModel =
    Encode.object
        [ ( "intro", Encode.string convModel.intro )
        , ( "author", Encode.string convModel.author )
        , ( "messages", Encode.list encodeChatMessage convModel.messages )
        , ( "participants", Encode.list Encode.string convModel.participants )
        , ( "author", Encode.string convModel.author )
        , ( "draftMessage", Encode.string convModel.draftMessage )
        , ( "convoId", Encode.string convModel.draftMessage )
        ]
