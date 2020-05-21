import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class visual extends PApplet {


final Point origin = new Point(0, 0),
  max = new Point(600, 600), 
  leftAxis = new Point(42.3f/2, 42.3f/2),
  rightAxis = new Point(600-42.3f/2, 42.3f/2);

final Rectangle layer1 = new Rectangle(origin, max, 0xffFAC767), 
  layer2 = new Rectangle(new Point(origin.x+42.3f, origin.y+42.3f), new Point(max.x-42.3f, max.y-42.3f), 0xffE3C895), 
  stepper1 = new Rectangle(origin, new Point(42.3f, 42.3f), 0xffA2A2A2), 
  stepper2 = new Rectangle(new Point(max.x-42.3f, origin.y), new Point(max.x, 42.3f), 0xffA2A2A2), 
  portrait = new Rectangle(new Point(151.5f, 90), new Point(max.x-151.5f, max.y-90), 0xffFFFFFF), 
  landscape = new Rectangle(new Point(90, 151.5f), new Point(max.x-90, max.y-151.5f), 0xffFFFFFF);

Ellipse left = new Ellipse(leftAxis), right = new Ellipse(rightAxis);

String fileName = "gCodes/Mickey_Mouse_GCODE.txt";
int displacementX;
int displacementY;
boolean portraitMode;
String[] gcodes;

ArrayList<Integer> lefts = new ArrayList<Integer>();
ArrayList<Integer> initialRights = new ArrayList<Integer>();
ArrayList<Integer> finalRights = new ArrayList<Integer>();
ArrayList<Point> points = new ArrayList<Point>();
int index = 0;

public void setup() {
  
  leftAxis.setC(0xffFF0000);
  rightAxis.setC(0xff0000FF);
  left.setC(0xffFF0000);
  right.setC(0xff0000FF);

  gcodes = loadStrings(fileName);
  parseTabs();
  addPoints();
}


public void draw() {
  //if(false) {
  if (index < points.size()) {
    //while drawing draw...
    
    //static backgrounds
    background(255);
    layer1.display();
    layer2.display();
    stepper1.display();
    stepper2.display();
    if(portraitMode) {
      portrait.display();
    }else {
      landscape.display();
    }
    leftAxis.display();
    rightAxis.display();
    
    //drawing - black
    for (int i = 0; i <= index; i++) {
      points.get(i).display();
    }
    
    //ellipses - red and blue
    int leftRad = (int)(sqrt(pow((points.get(index).x - leftAxis.x - displacementX), 2) + pow((points.get(index).y - leftAxis.y - displacementY), 2)) + 0.5f);
    int rightRad = (int)(sqrt(pow((points.get(index).x - rightAxis.x- displacementX), 2) + pow((points.get(index).y - rightAxis.y - displacementY), 2)) + 0.5f);
    left.setR(leftRad);
    right.setR(rightRad);
    left.display();
    right.display();
    
    //radiuses - red and blue
    stroke(0xffFF0000);
    strokeWeight(5);
    line(leftAxis.x, leftAxis.y, points.get(index).x - displacementX, points.get(index).y - displacementY);
    stroke(0xff0000FF);
    line(rightAxis.x, rightAxis.y, points.get(index).x - displacementX, points.get(index).y - displacementY);
    
    //displacements - green
    stroke(0xff00FF00);
    strokeWeight(5);
    line(points.get(index).x - displacementX, points.get(index).y - displacementY, points.get(index).x - displacementX, points.get(index).y);
    line(points.get(index).x - displacementX, points.get(index).y, points.get(index).x, points.get(index).y);
    System.out.println(leftRad + "   " + rightRad);
    index++;
  } else {
    //after it is done drawing
    //only display static backgrounds and drawing
    background(255);
    layer1.display();
    layer2.display();
    stepper1.display();
    stepper2.display();
    if(portraitMode) {
      portrait.display();
    }else {
      landscape.display();
    }
    leftAxis.display();
    rightAxis.display();
    for (Point point : points) 
      point.display();
  }
}

public void parseTabs() {
  
  displacementX = Integer.valueOf(gcodes[3].split("\t")[1]);
  displacementY = Integer.valueOf(gcodes[3].split("\t")[2]);
  
  portraitMode = gcodes[1].split("\t")[1].equals("portrait");
  
  for (int i = 9; i < gcodes.length; i++) {
    String[] commands = gcodes[i].split("\t");
    lefts.add(Integer.valueOf(commands[1]));
    initialRights.add(Integer.valueOf(commands[2]));
    finalRights.add(Integer.valueOf(commands[3]));
  }
}

public void addPoints() {
  for (int i = 0; i < lefts.size(); i++) {
    Ellipse left = new Ellipse(42.3f/2, 42.3f/2, lefts.get(i));
    Ellipse right = new Ellipse(width - 42.3f/2, 42.3f/2);


    if (initialRights.get(i) == finalRights.get(i)) {
    //draw one point
      right.setR(initialRights.get(i) );

      Point intersection = left.findIntersection(right);
      intersection.x += displacementX;
      intersection.y += displacementY;

      points.add(intersection);
      
      
    } else if (initialRights.get(i) < finalRights.get(i)) {
    //arc down
      for (int j = initialRights.get(i); j <= finalRights.get(i); j++) {
        //System.out.println("intial: " + initialRights.get(i) + " final: " + finalRights.get(i) + " current: " + j);
        right.setR(j);

        Point intersection = left.findIntersection(right);
        intersection.x += displacementX;
        intersection.y += displacementY;

        points.add(intersection);
      }
    } else {
    //arc up
      for (int j = initialRights.get(i); j > finalRights.get(i); j--) {
        //System.out.println("intial: " + initialRights.get(i) + " final: " + finalRights.get(i) + " current: " + j);
        right.setR(j);

        Point intersection = left.findIntersection(right);
        intersection.x += displacementX;
        intersection.y += displacementY;

        points.add(intersection);
      }
    }
  }
}
class Cluster {

  //private ArrayList<Point> points = new ArrayList<Point>();
  Ellipse left, innerRight, outerRight;

  Cluster(Ellipse left, Ellipse innerRight, Ellipse outerRight) {
    this.left = left;
    this.innerRight = innerRight;
    this.outerRight = outerRight;
  }

  //void display() {
  //  for (Point point : points) {
  //    point.display();
  //  }
  //}
}
class Ellipse {

  float x, y, r;
  int c;

  Ellipse(float x, float y) {
    this.x = x;
    this.y = y;
  }

  Ellipse(float x, float y, float r) {
    this(x, y);
    this.r = r;
  }
  
  Ellipse(Point point) {
    this(point.x, point.y);
  }
  
    public void setC(int c) {
    this.c = c;
  }

  public void display() {
    ellipseMode(RADIUS);
    stroke(c);
    strokeWeight(5);
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
  int c = 0;

  Point(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public void setC(int c) {
    this.c = c;
  }

  public void display() {
    stroke(c);
    strokeWeight(1.5f);
    point(x, y);
  }
}
class Rectangle {

  Point a, b, c, d;
  int col = 0;

  public Rectangle(Point a, Point b, Point c, Point d, int col) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
    this.col = col;
  }

  public Rectangle(Point a, Point d, int col) {
    this.a = a;
    this.d = d;
    b = new Point(d.x, a.y);
    c = new Point(a.x, d.y);
    this.col = col;
  }

  public void setColor(int col) {
    this.col = col;
  }

  public void display() {
    stroke(0xff000000);
    strokeWeight(1);
    fill(col);
    rect(a.x, a.y, b.x-a.x, c.y-a.y);
  }
}
  public void settings() {  size(600, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "visual" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
