package RFM_Miner;

import java.util.ArrayList;
import java.util.List;

public class RFUList {
     String item;
     double sumIutils=0.0;
     double sumRutils=0.0;
     double sumIfs=0.0;
     double sumRfs=0.0;
     double sumRvalue=0.0;

     List<Element> elements=new ArrayList<>();

   public RFUList() {
    }

    public RFUList(String item) {
        this.item = item;
    }
    //成员方法
    public void  addElement(Element element){
        sumIutils+= element.iutils;
        sumRutils+= element.rutils;
        sumIfs+= element.ifs;
        sumRfs+= element.rfs;
        sumRvalue+= element.Rvalue;
        elements.add(element);//一条element有项，iutil，rutil
    }

    public int getSupport(){
        return elements.size();
    }


}
