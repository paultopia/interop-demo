(ns thumbnail.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:import [org.imgscalr Scalr]
           [java.awt.image BufferedImageOp]
           [javax.imageio ImageIO]))

;; (set! *warn-on-reflection* true)

(defn read-image [filename]
  (ImageIO/read (io/file filename)))

(defn scale [image]
  (Scalr/resize image 200 (into-array BufferedImageOp []))) ; a bizarre quirk, see https://stackoverflow.com/a/18501367/4386239

(defn -main
  [infile outfile]
  (let [image (read-image infile)
        scaled (scale image)]
    (ImageIO/write scaled "jpg" (io/file outfile))
    (println "done!")))
