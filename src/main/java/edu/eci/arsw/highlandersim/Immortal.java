package edu.eci.arsw.highlandersim;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;

    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean locked = false;

    private ControlFrame k;

    private boolean muerto = false;

    private boolean fin = false;

    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
    }

    public void run() {
        if (immortalsPopulation.size() == 1) {
            termina();
            
        }

        while (!muerto && !fin) {
            if (!locked) {
                if (health == 0) {
                    muerto = true;
                    immortalsPopulation.remove(this);
                    break;
                }
                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                synchronized (k) {
                    try {
                        k.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

    }

    public void fight(Immortal i2) {
        if (this.hashCode() < i2.hashCode()) {
            synchronized (this) {
                synchronized (i2) {
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                    } else {
                        updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                    }

                }
            }
        }else{
            synchronized (i2) {
                synchronized (this) {
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
                    } else {
                        updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                    }

                }
            }
            
        }

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public void bloquear() {
        locked = true;
    }

    public void desbloquear() {
        synchronized (k) {
            k.notify();
        }
        locked = false;
    }

    public ImmortalUpdateReportCallback getUpdateCallback() {
        return updateCallback;
    }

    public void setUpdateCallback(ImmortalUpdateReportCallback updateCallback) {
        this.updateCallback = updateCallback;
    }

    public int getDefaultDamageValue() {
        return defaultDamageValue;
    }

    public void setDefaultDamageValue(int defaultDamageValue) {
        this.defaultDamageValue = defaultDamageValue;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public ControlFrame getK() {
        return k;
    }

    public void setK(ControlFrame k) {
        this.k = k;
    }

    public boolean isMuerto() {
        return muerto;
    }

    public void setMuerto(boolean muerto) {
        this.muerto = muerto;
    }

    public boolean isFin() {
        return fin;
    }

    public void setFin(boolean fin) {
        this.fin = fin;
    }
    
    public void termina(){
        fin=true;
    }

}
