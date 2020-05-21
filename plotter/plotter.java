import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Collections; 
import java.util.Set; 
import java.util.HashSet; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class plotter extends PApplet {






private final float stpCenter = 42.3f/2;
private final Point stepper1 = new Point(-90 + stpCenter, -151.5f + stpCenter), 
  stepper2 = new Point(420 + 90 - stpCenter, -151.5f + stpCenter), 
  center = new Point(420/2, 297/2), 
  ending = new Point (420/2, 0);

private final int radiusDis = 5;
private String pictureName = "Mickey_Mouse";
private final int displacementX = 0;
private final int displacementY = 10;
private final int limit = 2;
private final int stepperDiameter = 15;
private final int centerLeftRadius = PApplet.parseInt(sqrt((pow(center.x - stepper1.x, 2)) + (pow(center.y - stepper1.y, 2))) + 0.5f);
private final int centerRightRadius = PApplet.parseInt(sqrt((pow(center.x - stepper2.x, 2)) + (pow(center.y - stepper2.y, 2))) + 0.5f);
private final int endingLeftRadius = PApplet.parseInt(sqrt(pow(ending.x - stepper1.x, 2) + pow(ending.y - stepper1.y, 2)) + 0.5f);
private final int endingRightRadius = PApplet.parseInt(sqrt(pow(ending.x - stepper2.x, 2) + pow(ending.y - stepper2.y, 2)) + 0.5f);

private PImage img;
private int[] pxs;
private ArrayList<Ellipse> lefts = new ArrayList<Ellipse>();
private ArrayList<Ellipse> rights = new ArrayList<Ellipse>();
private ArrayList<Cluster> clusters = new ArrayList<Cluster>();
private ArrayList<String> gcodes = new ArrayList<String>();

public void setup() {
  

  img = loadImage("pictures/" + pictureName + ".png");
  img.loadPixels();
  pxs = img.pixels;

  for (float i = 100; i <= 750; i+=radiusDis)
    lefts.add(new Ellipse(stepper1.x, stepper1.y, i));

  for (int i = 100; i <= 750; i++)
    rights.add(new Ellipse(stepper2.x, stepper2.y, i));

  findTracings();

  String gcode;
  int command = 1;
  gcodes.add("ProjectName:\t" + pictureName);
  gcodes.add("paperMode:\tlandscape\t" + width + "\t" + height);
  gcodes.add("centering:\t" + centerLeftRadius + "\t" + centerRightRadius);
  gcodes.add("displacements:\t" + displacementX + "\t" + displacementY);
  gcodes.add("diameter:\t" + stepperDiameter);
  gcodes.add("distanceApart:\t" + radiusDis);
  gcodes.add("");
  gcodes.add("command\tleft\tiRight\tfRight");

  for (Cluster cluster : clusters) {
    int rL = (int)cluster.left.r;
    int rIR = (int)cluster.innerRight.r;
    int rOR = (int)cluster.outerRight.r;
    gcode = command + "\t" + rL + "\t" + rIR + "\t" + rOR; 
    gcodes.add(gcode);
    command++;
  }
  String[] forText = gcodes.toArray(new String[gcodes.size()]);
  saveStrings("gCodes/" + pictureName + "_GCODE.txt", forText);
}

public void draw() {
  image(img, 0, 0);
  for (Ellipse left : lefts)
    left.display();

  //float disx1 = stepper1.x - mouseX;
  //float disy1 = stepper1.y - mouseY;
  //float dis1 = sqrt(disx1*disx1 + disy1*disy1);
  //float disx2 = stepper2.x - mouseX;
  //float disy2 = stepper2.y - mouseY;
  //float dis2 = sqrt(disx2*disx2 + disy2*disy2);
  //Ellipse l = new Ellipse(stepper1.x, stepper1.y, dis1);
  //Ellipse r = new Ellipse(stepper2.x, stepper2.y, dis2);
  //l.display();
  //r.display();
  stroke(255, 0, 0);
  strokeWeight(3);
  ellipseMode(RADIUS);
  ellipse(stepper1.x, stepper1.y, centerLeftRadius, centerLeftRadius);
  ellipse(stepper2.x, stepper2.y, centerRightRadius, centerRightRadius);
  center.setC(0xff00FF00);
  center.display();

  stroke(0, 255, 0);
  strokeWeight(3);
  ellipseMode(RADIUS);
  ellipse(stepper1.x, stepper1.y, endingLeftRadius, endingLeftRadius);
  ellipse(stepper2.x, stepper2.y, endingRightRadius, endingRightRadius);
  ending.setC(0xffFF0000);
  ending.display();
  System.out.println(endingLeftRadius + "  " + endingRightRadius);
}



public void findTracings() {
  for (Ellipse left : lefts) {
    Ellipse innerRight = null, outerRight = null;

    if (lefts.indexOf(left)%2 == 0) {



      for (int i = 0; i < rights.size(); i++) {

        if (innerRight == null) {

          Ellipse right = rights.get(i);
          Point intersection = left.findIntersection(right);

          if (intersection != null) {
            intersection.x += displacementX;
            intersection.y += displacementY;

            if (intersection.x < width-limit && intersection.x > limit)
              if (intersection.y < height-limit && intersection.y > limit)
                if (getPixelColor(intersection) <= 10) {
                  innerRight = right;
                }
          }
        } else if (outerRight == null) {

          Ellipse right = rights.get(i);
          Point intersection = left.findIntersection(right);

          if (intersection != null) {
            intersection.x += displacementX;
            intersection.y += displacementY;

            if (intersection.x < width-limit && intersection.x > limit)
              if (intersection.y < height-limit && intersection.y > limit)
                if (getPixelColor(intersection) >= 250) {
                  outerRight = rights.get(i-1);
                }
          }
        }
        if (innerRight != null && outerRight != null) {
          clusters.add(new Cluster(left, innerRight, outerRight));
          innerRight = null;
          outerRight = null;
        } /*else if (innerRight != null && outerRight == null) {
         
         Point intersection = left.findIntersection(rights.get(i));
         intersection.x += displacementX;
         intersection.y += displacementY;
         
         if (getPixelColor(intersection) == -1) {
         outerRight = rights.get(i-1);
         clusters.add(new Cluster(left, innerRight, outerRight));
         innerRight = null;
         outerRight = null;
         }
         }*/
      }
    } else {





      for (int i = rights.size()-1; i >= 0; i--) {




        if (outerRight == null) {

          Ellipse right = rights.get(i);
          Point intersection = left.findIntersection(right);

          if (intersection != null) {
            intersection.x += displacementX;
            intersection.y += displacementY;

            if (intersection.x < width-limit && intersection.x > limit)
              if (intersection.y < height-limit && intersection.y > limit)
                if (getPixelColor(intersection) <= 10) {
                  outerRight = right;
                }
          }
        } else if (innerRight == null) {

          Ellipse right = rights.get(i);
          Point intersection = left.findIntersection(right);

          if (intersection != null) {
            intersection.x += displacementX;
            intersection.y += displacementY;

            if (intersection.x < width-limit && intersection.x > limit)
              if (intersection.y < height-limit && intersection.y > limit)
                if (getPixelColor(intersection) >= 250) {
                  innerRight = rights.get(i+1);
                }
          }
        }
        if (innerRight != null && outerRight != null) {
          clusters.add(new Cluster(left, outerRight, innerRight));
          innerRight = null;
          outerRight = null;
        }/* else if (outerRight != null && innerRight == null) {
         
         Point intersection = left.findIntersection(rights.get(i));
         intersection.x += displacementX;
         intersection.y += displacementY;
         
         if (getPixelColor(intersection) == -1) {
         innerRight = rights.get(i+1);
         clusters.add(new Cluster(left, outerRight, innerRight));
         innerRight = null;
         outerRight = null;
         }
         }*/
      }
    }
  }
}

public int getPixelColor(Point point) {
  final float rgb = pow(256, 3);
  final float white = 255.0f;
  float value;
  try {
    value = pxs[PApplet.parseInt(point.x + 0.5f) + PApplet.parseInt(point.y + 0.5f)*width];
  } 
  catch (IndexOutOfBoundsException e) {
    return -1;
  }
  value += rgb;
  value *= white / rgb;
  value += 0.5f;
  return (int)value;
}
class Cluster {

  private ArrayList<Point> points = new ArrayList<Point>();
  Ellipse left, innerRight, outerRight;

  Cluster(Ellipse left, Ellipse innerRight, Ellipse outerRight) {
    this.left = left;
    this.innerRight = innerRight;
    this.outerRight = outerRight;
  }

  public void display() {
    for (Point point : points) {
      point.display();
    }
  }
}
class Ellipse {

  float x, y, r;

  Ellipse(float x, float y) {
    this.x = x;
    this.y = y;
  }

  Ellipse(float x, float y, float r) {
    this(x, y);
    this.r = r;
  }

  public void display() {
    ellipseMode(RADIUS);
    stroke(0, 0, 255);
    strokeWeight(1);
    noFill();
    ellipse(x, y, r, r);
  }

  public void setR(float r) {
    this.r = r;
  }

  public Point findIntersection(Ellipse c2) {
    float d = sqrt(pow((c2.x - x), 2) + pow((c2.y - y), 2));

    float a = (pow(r, 2) - pow(c2.r, 2) + pow(d, 2)) / (2 * d);

    float h = sqrt(pow(r, 2) - pow(a, 2));

    float px = x + a * (c2.x - x) / d;
    float py = y + a * (c2.y - y) / d;

    float p1x = px + h * (c2.y - y) / d;
    float p1y = py - h * (c2.x - x) / d;

    float p2x = px - h * (c2.y - y) / d;
    float p2y = py + h * (c2.x - x) / d;

    if (d > r + c2.r) {
      return null;
    } else if (p1y < 0) {
      return new Point(p2x, p2y);
    } else {
      return new Point(p1x, p1y);
    }
  }
}
class Point {

  float x, y;
  int c = 255;

  Point(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public void setC(int c) {
    this.c = c;
  }

  public void display() {
    stroke(c);
    strokeWeight(5);
    point(x, y);
  }
}
  public void settings() {  size(420, 297); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "plotter" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
