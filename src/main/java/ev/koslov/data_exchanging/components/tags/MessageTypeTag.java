package ev.koslov.data_exchanging.components.tags;

/**
 * Taglib that describes {@link ev.koslov.data_exchanging.components.Message} type (request, response and its source and target).
 */
public enum MessageTypeTag {
    SERVER_TO_CLIENT_REQUEST,
    CLIENT_TO_SERVER_REQUEST,
    CLIENT_TO_CLIENT_REQUEST,

    SERVER_TO_CLIENT_RESPONSE,
    CLIENT_TO_SERVER_RESPONSE,
    CLIENT_TO_CLIENT_RESPONSE,
}
