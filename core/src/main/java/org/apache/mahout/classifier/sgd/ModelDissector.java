/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.classifier.sgd;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.apache.mahout.classifier.AbstractVectorClassifier;
import org.apache.mahout.math.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Uses sample data to reverse engineer a feature-hashed model.
 *
 * The result gives approximate weights for features and interactions
 * in the original space.
 */
public class ModelDissector {
  private Map<String,Vector> weightMap;

  public ModelDissector(int n) {
    weightMap = Maps.newHashMap();
  }

  public void update(Vector features, Map<String, Set<Integer>> traceDictionary, AbstractVectorClassifier learner) {
    features.assign(0);
    for (String feature : traceDictionary.keySet()) {
      if (!weightMap.containsKey(feature)) {
        for (Integer where : traceDictionary.get(feature)) {
          features.set(where, 1);
        }

        Vector v = learner.classifyNoLink(features);
        weightMap.put(feature, v);

        for (Integer where : traceDictionary.get(feature)) {
          features.set(where, 0);
        }
      }
    }

  }

  public List<Weight> summary(int n) {
    PriorityQueue<Weight> pq = new PriorityQueue<Weight>();
    for (String s : weightMap.keySet()) {
      pq.add(new Weight(s, weightMap.get(s)));
      while (pq.size() > n) {
        pq.poll();
      }
    }
    List<Weight> r = Lists.newArrayList(pq);
    Collections.sort(r, Ordering.natural().reverse());
    return r;
  }

  public static class Weight implements Comparable<Weight> {
    private String feature;
    private double value;
    private int maxIndex;
    private Vector weights;

    public Weight(String feature, Vector weights) {
      this.weights = weights;
      this.feature = feature;
      value = weights.norm(1);
      maxIndex = weights.maxValueIndex();
    }

    @Override
    public int compareTo(Weight other) {
      int r = Double.compare(this.value, other.value);
      if (r != 0) {
        return r;
      } else {
        return feature.compareTo(other.feature);
      }
    }

    public String getFeature() {
      return feature;
    }

    public double getWeight() {
      return value;
    }

    public int getMaxImpact() {
      return maxIndex;
    }
  }
}
