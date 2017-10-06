(ns thumbnail.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:import [org.imgscalr Scalr]
           [java.awt.image BufferedImageOp]
           [javax.imageio ImageIO]))

(defn read-image [filename]
  (ImageIO/read (io/file filename)))

(defn scale [image]
  (Scalr/resize image 200 (into-array BufferedImageOp [])))

(defn -main
  [infile outfile]
  (let [image (read-image infile)
        scaled (scale image)]
    (ImageIO/write scaled "jpg" (io/file outfile))))
