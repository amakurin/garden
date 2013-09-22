(ns garden.util
  "Utility functions used by Garden."
  (:require [clojure.string :as string]
            [garden.types]
            #+cljs [goog.string :as gstring]
            #+cljs [goog.string.format])
  (:import garden.types.CSSUnit
           garden.types.CSSImport
           garden.types.CSSMediaQuery
           garden.types.CSSKeyframes))

#+cljs
(defn format
  "Formats a string using goog.string.format."
  [fmt & args]
  (apply gstring/format fmt args))

(defprotocol ToString
  (^String to-str [this] "Convert a value into a string."))

(extend-protocol ToString
  #+clj clojure.lang.Keyword
  #+cljs Keyword
  (to-str [this] (name this))

  #+clj Object
  #+cljs default
  (to-str [this] (str this))

  nil (to-str [this] ""))

(defn ^String as-str
  "Convert a variable number of values into strings."
  [& args]
  (apply str (map to-str args)))

;;;; Inspection
 
(defn record?
  "Return true if obj is an instance of clojure.lang.IRecord."
  [x]
  #+clj (instance? clojure.lang.IRecord x)
  #+cljs (satisfies? IRecord x))
 
(defn hash-map?
  "Return true if obj is a map but not a record."
  [x]
  (and (map? x) (not (record? x))))

(def rule? vector?)

(def declaration? hash-map?)

(defn media-query?
  [x]
  (instance? CSSMediaQuery x))

(defn keyframes?
  [x]
  (instance? CSSKeyframes x))

(defn import?
  [x]
  (instance? CSSImport x))

(defn natural?
  "True if n is a natural number."
  [n]
  (and (integer? n) (pos? n)))

(defn between?
  "True if n is a number between a and b."
  [n a b]
  (let [bottom (min a b)
        top (max a b)]
    (and (>= n bottom) (<= n top))))

(defn wrap-quotes
  "Wrap a string with double quotes."
  [s]
  (str \" s \"))

(defn space-join
  "Return a space separated list of values. Subsequences are joined with
   commas."
  [xs]
  (string/join " " (map to-str xs)))

(defn comma-join
  "Return a comma separated list of values. Subsequences are joined with
   spaces."
  [xs]
  (let [ys (for [x xs] (if (sequential? x) (space-join x) (to-str x)))]
    (string/join ", " ys)))

(defn without-meta
  "Return obj with meta removed."
  [obj]
  (with-meta obj nil))


(defn clip
  "Return a number such that n is no less than a and no more than b."
  [a b n]
  (let [[a b] (if (<= a b) [a b] [b a])] 
    (max a (min b n))))

(defn average
  "Return the average of two or more numbers."
  [n m & more]
  (/ (apply + n m more) (+ 2.0 (count more))))

(defn into!
  "The same as `into` but for transient vectors."
  [coll xs]
  (loop [coll coll xs xs]
    (if-let [x (first xs)]
      (recur (conj! coll x) (next xs))
      coll)))

(defn prefix
  "Attach a CSS style prefix to s."
  [p s]
  (let [p (to-str p)]
    (if (= \- (last p))
      (str p s)
      (str p \- s))))

(defn vendor-prefix
  "Attach a CSS vendor prefix to s."
  [p s]
  (let [p (to-str p)]
    (if (= \- (first p))
      (prefix p s) 
      (prefix (str \- p) s))))

;; Taken from clojure.math.combinatorics.
(defn cartesian-product
  "All the ways to take one item from each sequence."
  [& seqs]
  (let [v-original-seqs (vec seqs)
	step
	(fn step [v-seqs]
	  (let [increment
		(fn [v-seqs]
		  (loop [i (dec (count v-seqs)), v-seqs v-seqs]
		    (if (= i -1) nil
			(if-let [rst (next (v-seqs i))]
			  (assoc v-seqs i rst)
			  (recur (dec i) (assoc v-seqs i (v-original-seqs i)))))))]
	    (when v-seqs
	       (cons (map first v-seqs)
		     (lazy-seq (step (increment v-seqs)))))))]
    (when (every? seq seqs)
      (lazy-seq (step v-original-seqs)))))
