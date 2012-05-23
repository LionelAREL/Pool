package pool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PoolPanel extends JPanel implements ActionListener, Comparator {
    Ball[] balls; // Contains all the balls on the table. Cueball always found at balls[0].
    boolean gball; // True when a ghostball needs to be drawn.
    int gcx;
    int gcy; // location of center of ghostball.
    double pocketsize;
    Ball wob; // the object ball that the ghost ball will be drawn next to
    int gballx; // location of ghostball (for drawing) (top right hand corner)
    int gbally;
    int numberofballs; // number of balls currently on the table
    Ball cueball;
    double tval;
    int ticks;
    PriorityQueue<Collision> collisions;
    private double tillnframe;
    public PoolPanel(){
	setBackground(Color.GREEN);
	setPreferredSize(new Dimension(800,600));
	numberofballs = 1;
	pocketsize = 1.5;
	balls = new Ball[16];
	cueball = new Ball(Color.WHITE, 500, 400, 0, 0, 42);
	collisions = new PriorityQueue(16, this);
	balls[0] = cueball;
        ticks = 0;
	Timer timer = new Timer(15, this);
	timer.start();
	tval = 0;
    }
    
    public int compare(Object a, Object b) {
	double val =  ((Collision)a).time - ((Collision)b).time;
	if(val < 0) {
	    return -1;
	} else if (val > 0) {
	    return 1;
	} else {
	    return 0;
	}
    }

    public boolean equals(Object obj) {
	return true;
    }
    
    @Override public void paintComponent(Graphics g){
	super.paintComponent(g);
	g.setColor(Color.BLACK);
	int height = getHeight();
	int width = getWidth();
	if (gball){
	    g.fillOval(gballx, gbally, cueball.size, cueball.size);
	    int cenx = (int)(wob.xp+wob.size/2);
	    int ceny = (int)(wob.yp+wob.size/2);
	    int vgx = (int)(cenx - gcx);
	    int vgy = (int)(ceny - gcy);
	    g.drawLine(gcx, gcy, gcx + 15*vgx, gcy + 15*vgy); 
	}
	g.drawLine(0, (int)(pocketsize*cueball.size), (int)(pocketsize*cueball.size), 0);
	g.drawLine(0, (int)height - (int)(pocketsize*cueball.size), (int)(pocketsize*cueball.size), height);
	g.drawLine((int)(width - pocketsize*cueball.size), height, width, (int)(height-pocketsize*cueball.size));
	g.drawLine((int)(width - pocketsize*cueball.size), 0, width, (int)(pocketsize*cueball.size));
	int cx = (int)(cueball.xp + cueball.size/2);
	int cy = (int)(cueball.yp + cueball.size/2);
	int count = 0;
	while(count < numberofballs){
	    Ball temp = balls[count];
	    g.setColor(temp.color);
	    g.fillOval((int)temp.xp, (int)temp.yp, temp.size, temp.size);
	    count++; 
	}
	int c2 = 0;
	g.setColor(Color.BLACK);
	if(cueball.xv == 0 && cueball.yv == 0){
	    g.drawLine( (int)(cx - cueball.ycue*cueball.size/2) , (int)(cy + cueball.xcue*cueball.size/2) , (int)(cx + cueball.xcue*600 - cueball.ycue*cueball.size/2) , (int)(cy + cueball.ycue*600 + cueball.xcue*cueball.size/2) );
	    g.drawLine(cx, cy, (int)(cx+(cueball.xcue*600)), (int)(cy+(cueball.ycue*600)));
	    g.drawLine( (int)(cx + cueball.ycue*cueball.size/2) , (int)(cy - cueball.xcue*cueball.size/2) , (int)(cx + cueball.xcue*600 + cueball.ycue*cueball.size/2) , (int)(cy + cueball.ycue*600 - cueball.xcue*cueball.size/2) );
	}
	g.fillOval(3*-cueball.size/2, 3*-cueball.size/2, 3*cueball.size, 3*cueball.size);
	g.drawString(Double.toString(tval), 100, 100);
    }
    
    public void detectCollisions() {
	tillnframe = 1;
	int count = 0;
	while (count < numberofballs){
	    Ball temp1 = balls[count];
	    int count2 = count + 1;
	    while(count2 < numberofballs){
		
		Ball temp2 = balls[count2];
		// a b c are the terms of a quadratic.  at^2 + bt + c  This code uses the quadratic equation to check for collisions.
		double a = ( (temp2.xv) * (temp2.xv) + (temp1.xv) * (temp1.xv) - 2*(temp2.xv)*(temp1.xv) +
			     (temp2.yv)*(temp2.yv) + (temp1.yv) * (temp1.yv) - 2*(temp2.yv)*(temp1.yv) );
		
		double b = 2 * ( (temp2.getcx() * temp2.xv) + (temp1.getcx() * temp1.xv) - (temp2.getcx() * temp1.xv) -
				 (temp1.getcx() * temp2.xv) + (temp2.getcy() * temp2.yv) + (temp1.getcy() * temp1.yv) - 
				 (temp2.getcy() * temp1.yv) - (temp1.getcy() * temp2.yv) );
		
		double c = temp2.getcx() * temp2.getcx() + temp1.getcx() * temp1.getcx() - 2 * (temp1.getcx() * temp2.getcx()) +
		    temp2.getcy() * temp2.getcy() + temp1.getcy() * temp1.getcy() - 2 * (temp1.getcy() * temp2.getcy())
		    - (temp1.size+temp2.size)*(temp1.size+temp2.size)/4;
                    double dist = Math.sqrt( (temp1.getcx()-temp2.getcx())*(temp1.getcx()-temp2.getcx())   +  (temp1.getcy()-temp2.getcy())*(temp1.getcy()-temp2.getcy()) );
		double t;
		if (a !=0 ){
		    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);  // These are the two solutions to the quadratic equation.
		    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);  // The smaller solution is always selected (unless it is
		    t = t1 < t2 ? t1 : t2;
		} else {
                    t = -c/b;  
                }
                if(t < tillnframe && 0 <= t){
                    collisions.add(new Collision(t, temp1, temp2));
		} else {
		    tval = t;
                    if(Math.abs(t) < 3) {
                        temp1 = temp2;
                    }
		}
		count2++;
	    }
            count++;
        } 
    }
    
    public void collisionEffects() {
	Collision coll = collisions.poll();
        while(coll != null) {
	    double time = coll.time;
	    Ball tempa = coll.ball1;
	    Ball tempb = coll.ball2;
	    tempa.xp = tempa.xp + time * tempa.xv;
	    tempa.yp = tempa.yp + time * tempa.yv;
	    tempb.xp = tempb.xp + time * tempb.xv;
	    tempb.yp = tempb.yp + time * tempb.yv;
	    double xdif = tempb.getcx() - tempa.getcx();
	    double ydif = tempb.getcy() - tempa.getcy();
	    double dist = Math.sqrt(xdif*xdif + ydif*ydif);
	    double xp = xdif/dist;
	    double yp = ydif/dist;
	    double xo = -yp;
	    double yo = xp;
	    double vp1 = xp * tempa.xv + yp * tempa.yv;
	    double vp2 = xp * tempb.xv + yp * tempb.yv;
	    double vo1 = xo * tempa.xv + yo * tempa.yv;
	    double vo2 = xo * tempb.xv + yo * tempb.yv;
	    tempa.xv = vp2 * xp - vo1 * yp;
	    tempa.yv = vp2 * yp + vo1 * xp;
	    tempb.xv = vp1 * xp - vo2 * yp;
	    tempb.yv = vp1 * yp + vo2 * xp;
	    tillnframe = tillnframe - time;
	    int count = 0;
	    while(count < numberofballs){
		Ball temp = balls[count];
		if (!(temp == tempa) && !(temp == tempb)){
		    temp.xp = temp.xp + temp.xv * time;
		    temp.yp = temp.yp + temp.yv * time;
		}
		count++;
	    }
            coll = collisions.poll();
	}

    }

    public void updateGhostBall() {
	int count;
        double tmin = 2000;
	for(count = 1; count < numberofballs; count ++) {
	    Ball temp = balls[count];
	    if(cueball.xv == 0 && cueball.yv == 0 && ! (temp==cueball)){
		// Quadratic with solutions (similar to collision detection)
		double a = cueball.xcue*cueball.xcue + cueball.ycue*cueball.ycue;
		double b = 2 * (cueball.getcx()*cueball.xcue - temp.getcx()*cueball.xcue + 
				cueball.getcy()*cueball.ycue - temp.getcy()*cueball.ycue);
		double c = cueball.getcx()*cueball.getcx() + temp.getcx()*temp.getcx() +
		    cueball.getcy()*cueball.getcy() + temp.getcy()*temp.getcy() - 2*cueball.getcx()*temp.getcx() - 
		    2*cueball.getcy()*temp.getcy() - (temp.size + cueball.size)*(temp.size + cueball.size)/4;
		double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
		double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
		double time = t1 < t2 ? t1 : t2;
		
		if( !(Double.isNaN(time)) && time < tmin && time > 0){
		    gball = true;
		    gballx = (int)(cueball.xp + time * cueball.xcue);
		    gbally = (int)(cueball.yp + time * cueball.ycue);
		    gcx = (int)(cueball.getcx() + time * cueball.xcue);
		    gcy = (int)(cueball.getcy() + time * cueball.ycue);
		    wob = temp; // sets temp to the object ball on which the ghostball is drawn
		    tmin = time;
		}
	    }
	}
    }
    
    
    public void actionPerformed(ActionEvent evt){
	int height = getHeight(); 
	int width = getWidth();
        ticks += 1;
	detectCollisions();
	collisionEffects();
	updateGhostBall();

	int count = 0;
	while (count < numberofballs) {
	    Ball temp = balls[count];
	    if( !(temp==cueball) && (
				     temp.xp+cueball.size + temp.yp+cueball.size <= pocketsize*cueball.size ||
				     temp.xp < -cueball.size || temp.xp > width || temp.yp> height || temp.yp < -cueball.size
				     )  ){
		int c = count;
		while(c<numberofballs-1){
		    balls[c] = balls[c+1];
		    c++;
		}
		temp = balls[count];
		numberofballs = numberofballs-1;
	    }
	    if( (width <= (temp.xp+temp.size) && temp.xv > 0) && !( temp.getcy() <= pocketsize*cueball.size ) && !(height - temp.getcy() <= pocketsize*cueball.size) ){
		temp.xv = -temp.xv;
	    }
	    if(temp.xp <= 0 && temp.xv < 0 && !( temp.getcy() <= pocketsize*cueball.size ) && !(height - temp.getcy() <= pocketsize*cueball.size) ){
		temp.xv = -temp.xv;
	    }
	    if(temp.yp <= 0 && temp.yv < 0 &&  !( temp.getcx() <= pocketsize*cueball.size ) && !(width - temp.getcx() <= pocketsize*cueball.size)){
		temp.yv = -temp.yv;
	    }
	    if(height <= (temp.yp + temp.size) && temp.yv > 0 && !( temp.getcx() <= pocketsize*cueball.size ) && !(width - temp.getcx() <= pocketsize*cueball.size) ){
		temp.yv = -temp.yv;
	    }
	  
	
	    temp.xp += temp.xv*tillnframe; 
            temp.yp += temp.yv*tillnframe; 


	    if (Math.abs(temp.xv) < .10 && Math.abs(temp.yv) < .10){
		temp.xv = 0;
		temp.yv = 0;
	    }
	    temp.xv = .99*temp.xv;  // Simulates friction by decreasing velocity slightly each frame.
	    temp.yv = .99*temp.yv;
	    count++;
	}
	this.repaint();
    }
}