package com.spotify.docker.it;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;

/**
 * Helper methods that creates JSON representation from objects and vice versa.
 *
 * @author Ivan Krizsan
 */
public final class JsonConverter {
    /**
     * Default constructor.
     * Made private to prevent instantiation of this helper class.
     */
    private JsonConverter() {
    }

    /**
     * Creates a JSON representation of supplied object hierarchy.
     *
     * @param inObjectToSerialize Object to create JSON representation from.
     * @param inPrettyPrintFlag Flag indicating whether to pretty print the
     * JSON string.
     * @return JSON representation of supplied object.
     * @throws Exception If error occurs creating JSON representation.
     */
    public static String objectToJson(final Object inObjectToSerialize,
        final boolean inPrettyPrintFlag)
        throws Exception {
        final ObjectMapper theJsonObjectMapper = createAndConfigureJsonObjectMapper(
            inPrettyPrintFlag);
        final ObjectWriter theJsonObjectWriter = theJsonObjectMapper.writer();
        final String theJsonString =
            theJsonObjectWriter.writeValueAsString(inObjectToSerialize);
        return theJsonString;
    }

    /**
     * Creates an object of supplied type from supplied JSON string.
     *
     * @param inJsonRepresentation JSON representation from which to create object(s).
     * @param inDestinationType Type of the (root) object to create.
     * @return Object(s) created from JSON representation.
     * @throws IOException If error occurs creating object(s).
     */
    public static <T> T jsonToObject(final String inJsonRepresentation,
        final Class<T> inDestinationType)
        throws IOException {
        final ObjectMapper theJsonObjectMapper = createAndConfigureJsonObjectMapper(
            false);
        final ObjectReader theJsonObjectReader = theJsonObjectMapper.readerFor(inDestinationType);
        return theJsonObjectReader.readValue(inJsonRepresentation);
    }

    /**
     * Creates and configures the object mapper used when converting between
     * JSON representation and objects.
     *
     * @param inPrettyPrintFlag Flag indicating whether to configure object
     * mmapper to pretty print when converting to an object hierarchy to a JSON
     * string.
     * @return Jackson object mapper.
     */
    protected static ObjectMapper createAndConfigureJsonObjectMapper(
        final boolean inPrettyPrintFlag) {
        final ObjectMapper theJsonObjectMapper = new ObjectMapper();
        theJsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        theJsonObjectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return theJsonObjectMapper;
    }
}
