package models;

import nn.Layer;

import org.jblas.DoubleMatrix;
import java.util.Iterator;
import java.util.ListIterator;

import common.Sentence;

public class AddModel extends Model {
  public AddModel() {
    super();
  }
  public DoubleMatrix fProp(Sentence sent) {
//    this.clearData();
    DoubleMatrix rep = DoubleMatrix.zeros(1,super.outSize);
    Iterator<Integer> sentIt = sent.words.iterator();
    while(sentIt.hasNext()) {
      DoubleMatrix input = this.dict.getRepresentation(sentIt.next());
      Iterator<Layer> layerIt = this.layers.iterator();
      DoubleMatrix temp = input;
      while(layerIt.hasNext()) {
        Layer l = layerIt.next();
        l.fProp(temp);
        temp = l.getActivities();
      }
      rep.addi(temp);
    }
    return rep;
  }

  public void bProp(Sentence s1, Sentence s2, boolean add) {
    // send s2 up -- use it as label
    // send s1 up 
    // calculate error (1-2)
    // backpropagate error
    DoubleMatrix label = this.fProp(s2);
    DoubleMatrix pred = this.fProp(s1);

    DoubleMatrix error = pred.sub(label);
    // This is important at this point because whenever you need to fProp, it better to clead the data to contains.
//    this.clearData();
    Iterator<Integer> sentIt = s1.words.iterator();
    while(sentIt.hasNext()) {
      DoubleMatrix input = this.dict.getRepresentation(sentIt.next());
      Iterator<Layer> layerIt = this.layers.iterator();
      DoubleMatrix temp = input;
      while(layerIt.hasNext()) {
        Layer l = layerIt.next();
        l.fProp(temp);
        temp = l.getActivities();
      }
      ListIterator<Layer> layerRevIt = this.layers.listIterator(this.layers.size());
      temp = error;
      while(layerRevIt.hasPrevious()) {
        Layer l = layerRevIt.previous();
        l.bProp(temp);
        l.accumulateGradients(add);
      // CHECK BIAS ERROR
        temp = l.getGradients().mmul(l.getWeights().transpose());
      }
    }
  }

}