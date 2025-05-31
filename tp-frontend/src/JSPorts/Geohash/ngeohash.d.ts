/**
 * Declaration file for the node geohash library functions we need to use
 * See https://github.com/sunng87/node-geohash/blob/master/main.js
 */

declare module 'ngeohash' {

    /**
     * Encodes a lattitue and longitude into a geohash string.
     * See {@link https://en.wikipedia.org/wiki/Geohash} 
     * @param lat Lattitude position
     * @param lon Longitude position
     * @param numberOfChars Number of characters to return, defaults to 9.
     *                      Can be though of as precision
     */
    export function encode(lat: number, lon: number, numberOfChars?: number): string;

}