;;;; Copyright © 2013 Paul Stadig.  All rights reserved.
;;;;
;;;; The use and distribution terms for this software are covered by the Eclipse
;;;; Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html) which can be
;;;; found in the file epl-v10.html at the root of this distribution.  By using
;;;; this software in any fashion, you are agreeing to be bound by the terms of
;;;; this license.
;;;;
;;;; You must not remove this notice, or any other, from this software.
(ns name.stadig.clojure-clojure
  (:refer-clojure :exclude [= cons count counted? doseq empty first hash list?
                            meta next peek pop reduce rest seq seq? sequential?
                            str with-meta])
  (:require [name.stadig.clojure-clojure.protocols :as proto])
  (:import (java.io Serializable)
           (java.util Arrays Collection Iterator List ListIterator
                      NoSuchElementException)))

(defn = [o p]
  (proto/equiv o p))

(defn cons [x s] (proto/cons s x))

(defn count [s] (proto/count s))

(defn counted? [o] (proto/counted? o))

(defn empty [s] (proto/empty s))

(defn first [s] (proto/first s))

(defn hash [o] (proto/hasheq o))

(defn list? [s] (proto/list? s))

(defn meta [o] (proto/meta o))

(defn next [s] (proto/next s))

(defn peek [s] (proto/peek s))

(defn pop [s] (proto/pop s))

(defn reduce
  ([f s]
     (proto/reduce s f))
  ([f init s]
     (proto/reduce s f init)))

(defn rest [s] (proto/more s))

(defn seq [s] (proto/seq s))

(defn seq? [s] (proto/seq? s))

(defn sequential? [s] (proto/sequential? s))

(defn str [s] (if (nil? s) "nil" (.toString s)))

(defn with-meta [o m] (proto/with-meta o m))

(defn- unsigned-bit-shift-right [x n]
  (let [shifted (bit-shift-right x n)
        negative? (bit-test shifted (- 64 n))]
    (if negative?
      (bit-and shifted (bit-shift-right Long/MAX_VALUE (dec n)))
      shifted)))

(defn- equals* [o p]
  (or (and (nil? o) (nil? p))
      (and o (.equals o p))))

(defn- hash-code* [o]
  (if (nil? o)
    0
    (.hashCode o)))

(defn- seq-equals* [s o]
  (cond
   (identical? s o)
   true
   (not= (hash-code* s) (hash-code* o))
   false
   (and (not (instance? List o)) (not (sequential? o)))
   false
   (or (not (counted? s)) (not (counted? o)) (not= (count s) (count o)))
   false
   :else
   (loop [s (seq s)
          o (seq o)]
     (if s
       (if (equals* (first s) (first o))
         (recur (next s) (next o))
         false)
       true))))

(defn- seq-equiv* [s o]
  (cond
   (and (not (instance? List o)) (not (sequential? o)))
   false
   (or (not (counted? s)) (not (counted? o)) (not= (count s) (count o)))
   false
   :else
   (loop [s (seq s)
          o (seq o)]
     (if s
       (if (= (first s) (first o))
         (recur (next s) (next o))
         false)
       true))))

(defn- seq-hash-code* [s]
  (loop [h 1
         s (seq s)]
    (if s
      (recur (+ (* 31 h) (hash-code* (first s))) (next s))
      h)))

(defn- seq-hasheq* [s]
  (loop [h 1
         s (seq s)]
    (if s
      (recur (+ (* 31 h) (hash (first s))) (next s))
      h)))

(defn- seq-to-string* [s]
  (let [sb (StringBuilder. "(")
        s (seq s)]
    (when s
      (.append sb (str (first s)))
      (loop [s (next s)]
        (when s
          (.append sb " ")
          (.append sb (str (first s)))
          (recur (next s)))))
    (.append sb ")")
    (.toString sb)))

(deftype PersistentListIterator [_list ^:volatile-mutable _index]
  ListIterator
  (add [this e] (throw (UnsupportedOperationException.)))
  (hasPrevious [this]
    (> _index 0))
  (nextIndex [this]
    (min (count _list) (inc _index)))
  (previous [this]
    (when (<= _index 0)
      (throw (NoSuchElementException.)))
    (let [index (dec _index)]
      (set! _index index)
      (.get _list index)))
  (previousIndex [this]
    (max 0 (inc _index)))
  (set [this e] (throw (UnsupportedOperationException.)))
  Iterator
  (hasNext [this]
    (< _index (count _list)))
  (next [this]
    (when (>= _index (count _list))
      (throw (NoSuchElementException.)))
    (let [index (inc _index)]
      (set! _index index)
      (.get _list index)))
  (remove [this] (throw (UnsupportedOperationException.))))

(declare empty-list)

(deftype PersistentList [_first _rest _count _meta]
  proto/IMeta
  (meta [this] _meta)
  proto/IObj
  (with-meta [this meta] (PersistentList. _first _rest _count meta))
  Serializable
  proto/IConsable
  (cons [this o] (PersistentList. o this (inc _count) _meta))
  proto/IEquivable
  (equiv [this o] (seq-equiv* this o))
  proto/IPersistentCollection
  (empty [this] empty-list)
  proto/Seqable
  (seq [this] this)
  proto/ISeq
  (first [this] _first)
  (more [this] _rest)
  (next [this] _rest)
  (seq? [this] true)
  proto/Sequential
  (sequential? [this] true)
  List
  (add [this i e] (throw (UnsupportedOperationException.)))
  (addAll [this i c] (throw (UnsupportedOperationException.)))
  (get [this i]
    (if (and (<= 0 i) (< i _count))
      (loop [s this
             j i]
        (if (> j 0)
          (recur (rest s) (dec j))
          (first s)))
      (throw (IndexOutOfBoundsException.))))
  (indexOf [this o]
    (loop [i 0]
      (if (< i _count)
        (if (equals* o (.get this i))
          i
          (recur (inc i)))
        -1)))
  (lastIndexOf [this o]
    (loop [last-idx -1
           i 0]
      (if (< i _count)
        (if (equals* o (.get this i))
          (recur i (inc i))
          (recur last-idx (inc i)))
        last-idx)))
  (listIterator [this]
    (.listIterator this -1))
  (listIterator [this i]
    (PersistentListIterator. this i))
  (remove [this ^int i] (throw (UnsupportedOperationException.)))
  (set [this i e] (throw (UnsupportedOperationException.)))
  (subList [this from to]
    (loop [sub-list empty-list
           i 0
           s this]
      (cond
       (< i from)
       (recur sub-list (inc i) (rest s))
       (and (>= i from) (< i to))
       (recur (cons (first s) sub-list) (inc i) (rest s))
       (>= i to)
       (loop [s sub-list
              sub-list empty-list]
         (if (seq s)
           (recur (rest s) (cons (first s) sub-list))
           sub-list)))))
  Collection
  (add [this e] (throw (UnsupportedOperationException.)))
  (addAll [this c] (throw (UnsupportedOperationException.)))
  (clear [this] (throw (UnsupportedOperationException.)))
  (contains [this o]
    (not (= -1 (.indexOf this o))))
  (containsAll [this c]
    (let [itr (.iterator c)]
      (loop [itr itr]
        (if (.hasNext itr)
          (if (not (.contains this (.next itr)))
            false
            (recur itr))
          true))))
  (isEmpty [this] false)
  (^boolean remove [this o] (throw (UnsupportedOperationException.)))
  (removeAll [this c] (throw (UnsupportedOperationException.)))
  (retainAll [this c] (throw (UnsupportedOperationException.)))
  (size [this] _count)
  (toArray [this]
    (.toArray this (object-array _count)))
  (toArray [this a]
    (if (< (alength a) _count)
      (let [a (Arrays/copyOf a _count)]
        (.toArray this a))
      (do (loop [f _first
                 r _rest
                 i 0]
            (if (< i _count)
              (do (aset a i f)
                  (recur (first r) (rest r) (inc i)))))
          (when (> (alength a) _count)
            (aset a _count nil))
          a)))
  Iterable
  (iterator [this]
    (.listIterator this))
  proto/IHashEq
  (hasheq [this] (seq-hasheq* this))
  proto/IPersistentList
  (list? [this] true)
  proto/IPersistentStack
  (peek [this] _first)
  (pop [this] _rest)
  proto/IReduce
  (reduce [this f]
    (.reduce _rest f _first))
  (reduce [this f init]
    (loop [result init
           s this]
      (if (seq s)
        (recur (f result (first s)) (rest s))
        result)))
  proto/Counted
  (count [this] _count)
  (counted? [this] true)
  Object
  (equals [this o] (seq-equals* this o))
  (hashCode [this] (seq-hash-code* this))
  (toString [this] (seq-to-string* this)))

(deftype EmptyPersistentList [_meta]
  proto/IMeta
  (meta [this] _meta)
  proto/IObj
  (with-meta [this meta] (EmptyPersistentList. meta))
  Serializable
  proto/IConsable
  (cons [this o] (PersistentList. o this 1 _meta))
  proto/IEquivable
  (equiv [this o]
    (if (or (instance? List o) (sequential? o))
      (cond
       (and (instance? List o) (= 0 (.size o))) true
       (and (counted? o) (= 0 (count o))) true
       (not (seq o)) true
       :else false)
      false))
  proto/IPersistentCollection
  (empty [this] empty-list)
  proto/Seqable
  (seq [this] nil)
  proto/ISeq
  (first [this] nil)
  (more [this] this)
  (next [this] nil)
  (seq? [this] true)
  proto/Sequential
  (sequential? [this] true)
  List
  (add [this i e] (throw (UnsupportedOperationException.)))
  (addAll [this i c] (throw (UnsupportedOperationException.)))
  (get [this i] (throw (IndexOutOfBoundsException.)))
  (indexOf [this o] -1)
  (lastIndexOf [this o] -1)
  (listIterator [this]
    (.listIterator this -1))
  (listIterator [this i]
    (PersistentListIterator. this i))
  (remove [this ^int i] (throw (UnsupportedOperationException.)))
  (set [this i e] (throw (UnsupportedOperationException.)))
  (subList [this from to]
    (loop [sub-list empty-list
           i 0
           s this]
      (cond
       (< i from)
       (recur sub-list (inc i) (rest s))
       (and (>= i from) (< i to))
       (recur (cons (first s) sub-list) (inc i) (rest s))
       (>= i to)
       (loop [s sub-list
              sub-list empty-list]
         (if (seq s)
           (recur (rest s) (cons (first s) sub-list))
           sub-list)))))
  Collection
  (add [this e] (throw (UnsupportedOperationException.)))
  (addAll [this c] (throw (UnsupportedOperationException.)))
  (clear [this] (throw (UnsupportedOperationException.)))
  (contains [this o]
    (not (= -1 (.indexOf this o))))
  (containsAll [this c]
    (let [itr (.iterator c)]
      (loop [itr itr]
        (if (.hasNext itr)
          (if (not (.contains this (.next itr)))
            false
            (recur itr))
          true))))
  (isEmpty [this] false)
  (^boolean remove [this o] (throw (UnsupportedOperationException.)))
  (removeAll [this c] (throw (UnsupportedOperationException.)))
  (retainAll [this c] (throw (UnsupportedOperationException.)))
  (size [this] 0)
  (toArray [this]
    (object-array 0))
  (toArray [this a]
    (if (> (alength a) 0)
      (aset a 0 nil))
    a)
  Iterable
  (iterator [this]
    (.listIterator this))
  proto/IHashEq
  (hasheq [this] 1)
  proto/IPersistentList
  (list? [this] true)
  proto/IPersistentStack
  (peek [this] nil)
  (pop [this] this)
  proto/IReduce
  (reduce [this f] this)
  (reduce [this f init] init)
  proto/Counted
  (count [this] 0)
  (counted? [this] true)
  Object
  (equals [this o]
    (if (and (instance? List o) (= 0 (.size o)))
      true
      false))
  (hashCode [this] 1)
  (toString [this] "()"))

(def empty-list (EmptyPersistentList. nil))
