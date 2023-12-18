import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Board extends JPanel implements ActionListener {

    private Timer timer;

    private  int SCREEN_HEIGHT ;
    private  int SCREEN_WIDTH ;
    private final int DINO_STANDING_HEIGHT = 47;
    private final int DINO_STANDING_WIDTH = 44;
    private final int DINO_DUCK_HEIGHT = 30;
    private final int DINO_DUCK_WIDTH = 59;
    private final  int DINO_Y_SPEED = 8;
    private final  int OBS_X_SPEED = 5;
    private int dino_width = DINO_STANDING_WIDTH;
    private int dino_height = DINO_STANDING_HEIGHT;
    private double dino_y = 0;
    private double dino_x = 10;
    private double obs_x,obs_y;
    private double dino_speed_y = DINO_Y_SPEED;
    private double obs_speed_x = OBS_X_SPEED;
    private  int JUMP_HEIGHT = dino_height *3 + dino_height/2;

    private int obs_height ,obs_width;
    private int Score = 1 ;
    private boolean haveObstacle = false;
    private boolean isCactus;
    private boolean isLarge;
    private BufferedImage [] cactus_images_display;
    private int resize = 10;
    private int large_cactus_width = 25;
    private  int large_cactus_height = 50;
    private int small_cactus_width = 17;
    private int small_cactus_height = 35;
    private int bat_width = 46;
    private int bat_heigth = 38;
    private final int MILLISEC_IMAGE_CHANGE = 100;
    private boolean jumped = false;
    private final int TIMER_DELAY = 5;

    private int LIMIT_OBSTACLE_NUMBER = 3;
    private Random randomGenerator = new Random();



    //animations

    private final int dino_anim_frame_ind = 0;
    private final int ducking_anim_frame_ind = 1;
    private final int bat_anim_frame_ind = 2;
    private int []animations_frame = {
            1,
            1,
            1
    };
    private int []animation_timers = {
            0,
            0,
            0
    };



    private background sky,ground;

    //x ,y ,width,height
    private  int ground_prop[];
    private  int  cloud_prop [] ;
    private boolean isStanding = true;
    private BufferedImage [] dino_current_animation;


    private long startTime = System.currentTimeMillis();
    private int accumulatedTime  = 0 ;
    private double object_passed_counter =0;

    private int score_pixel_width = 10;
    private  int score_pixel_height = 11;
    private int gameStatus = 0;
    // 0 = start
    // 1 = death
    //
    private  BufferedImage [] large_cactus_images, small_cactus_images,bat_images,dinosaur_images
            ,cloud_images, ground_images,dino_duck_images,score_images,dino_death_images;


    public Board(int width, int height){
        SCREEN_HEIGHT = height;
        SCREEN_WIDTH = width;
        ground_prop = new int[]{0,SCREEN_HEIGHT-10,100,12};
        cloud_prop = new int[] { 0 ,SCREEN_HEIGHT -dino_height*2,46,13};
        dino_y = SCREEN_HEIGHT - DINO_STANDING_HEIGHT;
        loadResources();
        initBoard();

    }
    private void initBoard() {

        setFocusable(true);

       addKeyListener(new TAdapter());

        setBackground(Color.white);

        timer = new Timer(TIMER_DELAY, this);
        Start();

    }
    public void Start(){
        gameStatus = 0 ;
        sky = new background(cloud_images,cloud_prop,false);
        ground = new background(ground_images,ground_prop,true);
        startTime = System.currentTimeMillis();
        Score = 1;
        accumulatedTime = 0 ;
        object_passed_counter = 0;
        stand();
        haveObstacle=false;
        Arrays.fill(animation_timers, 0);
        Arrays.fill(animations_frame, 1);

        timer.start();

    }



    public void moveBackGround(){
        sky.moveBackground(1);
        ground.moveBackground(obs_speed_x);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        dinoStatus();
        moveObstacle();
        moveBackGround();
        addScore();

        repaint();
        if(intersected()){
            gameStatus = 1;
            repaint();
            timer.stop();
            return;
        }


    }

    //movements of dinosaur
    public  void dinoStatus(){

        if(jumped){
            dino_y= dino_y - dino_speed_y;
        }

        if(reachedJumpLimit()) jumped = false;
        //falling
        if(!jumped && isInTheAir()){
            dino_y= dino_y + dino_speed_y;
            //reached the bottom of the screen
            if(!isInTheAir()){
                dino_y = SCREEN_HEIGHT - dino_height;
            }
        }

    }

    public void duck(){

        if(!isInTheAir()){
            isStanding = false;
            dino_height = DINO_DUCK_HEIGHT;
            dino_width=DINO_DUCK_WIDTH;
            dino_y = SCREEN_HEIGHT - dino_height;
        }

    }
    public  void stand(){
        isStanding = true;
        if(!isInTheAir()){

            dino_height = DINO_STANDING_HEIGHT;
            dino_width=DINO_STANDING_WIDTH;
            dino_y = SCREEN_HEIGHT - dino_height;
        }


    }
    public  void  jump(){

        if(!isInTheAir()){
            jumped = true;
            stand();
        }

    }
    public  boolean  isInTheAir(){

        return   !(dino_y + dino_height == SCREEN_HEIGHT);

    }
    public  boolean  reachedJumpLimit(){

        return   (dino_y   <= SCREEN_HEIGHT - (JUMP_HEIGHT));

    }

    public void addScore(){
        long difference = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();
        accumulatedTime += difference + obs_speed_x;
        int time_limit = 100 ;//in milisec
            if(accumulatedTime >= time_limit){
                accumulatedTime= accumulatedTime - time_limit;
                Score ++;
            }

    }

    private  void moveObstacle(){
        //if there is no current obstacle

    if(!haveObstacle){
        setObstacle();
        object_passed_counter++;
        haveObstacle =true;
    }

    if(object_passed_counter % (10+obs_speed_x) == 0  && obs_speed_x < 20){
        object_passed_counter=1;
        obs_speed_x+= 1;
    }

     obs_x = obs_x - obs_speed_x;
     if(obs_x + obs_width <0){
         haveObstacle =false;
     }


    }


    private  void setObstacle(){

        if(Math.random() >=0.4){
            createCatus();
        }else if (Score >= 500){
            createBat();
        }else{
            createCatus();
        }

    }




    public void  loadResources(){

        //large
        large_cactus_images = loadImages("image/large_cactus.png",large_cactus_width,large_cactus_height,0);
        //small
        small_cactus_images = loadImages("image/small_cactus.png",small_cactus_width,small_cactus_height,0);
        //bat
        bat_images = loadImages("image/bat.png",bat_width,bat_heigth,0);
        //dino

        dinosaur_images = loadImages("image/dinosaur.png",dino_width,dino_height,0);
        dino_death_images = loadImages("image/death.png",dino_width,dino_height,0);

        //cloud
        cloud_images = loadImages("image/cloud.png",cloud_prop[2],cloud_prop[3],0);
        //floor
        ground_images = loadImages("image/floor.png", ground_prop[2], ground_prop[3],0);

        dino_duck_images = loadImages("image/dino_duck.png", DINO_DUCK_WIDTH, DINO_DUCK_HEIGHT,0);

        score_images = loadImages("image/score.png", score_pixel_width, score_pixel_height,0);

    }
    public BufferedImage [] loadImages(String path , int width, int height ,int gap){
        try {
            File file = new File(path);
            BufferedImage whole_image = ImageIO.read(file);
            BufferedImage  holder[] = new BufferedImage[whole_image.getWidth()/width];
            processImage(whole_image,width,height,gap,holder);

            return  holder;

        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }

    }


    public void processImage(BufferedImage wholeImage, int width, int height,int gap, BufferedImage collection[]){

        for (int x = 0,counter = 0 ; x < wholeImage.getWidth(); x+= width +gap,counter++){

            BufferedImage subImgage = wholeImage.getSubimage(x, 0, width, height);

            collection[counter] =subImgage;

        }

    }
    public BufferedImage  getCactusImage(boolean isLarge){
        if (!isLarge){
            int randomIndex =  randomGenerator.nextInt(small_cactus_images.length);

            return small_cactus_images[randomIndex];
        }
        int randomIndex =  randomGenerator.nextInt(large_cactus_images.length);

        return large_cactus_images[randomIndex];
    }
    public boolean intersected(){

        int offset = 0;
        double obs_x = this.obs_x + offset;
        int obs_width = this.obs_width - offset;
        int obs_height = this.obs_width +offset;
        double obs_y = this.obs_y + offset;

         return    dino_x+dino_width >=obs_x+resize &&
                dino_x <= obs_x+obs_width- resize
                &&
                dino_y+dino_height >= obs_y+resize
                &&
                dino_y <= obs_y+obs_height- resize;

    }


    public void generateCactus(int set,int height){
        obs_width = 0;
        obs_height= height;
        cactus_images_display = new BufferedImage[set];



        boolean [] cactus_large = new boolean[set];
        cactus_large[0]= this.isLarge;
        cactus_large[cactus_large.length-1]= this.isLarge;
        //between
        for (int i = 1; i<set-1;i++){
            //large and small
            if(isLarge){
                cactus_large[i]    =randomGenerator.nextBoolean();
                continue;
            }
            //if its small only small
            cactus_large[i]    =false;

        }


        //get width
        for (int i = 0; i<cactus_large.length;i++){
            if(cactus_large[i])
                obs_width+= large_cactus_width;
            else    obs_width+= small_cactus_width;
        }

        obs_y = SCREEN_HEIGHT - obs_height ;
        obs_x =    SCREEN_WIDTH / Scaled_x;

        generateImage(cactus_large);


    }
    public void generateImage(boolean[] set ){

        for (int i = 0; i < set.length; i++) {
            cactus_images_display[i] = getCactusImage(set[i]);
        }


    }
    public  void  createCatus(){
        isCactus =true;
        this.isLarge =randomGenerator.nextBoolean();
        int set_of_catus = 0;
        int cactus_height;
        set_of_catus = randomGenerator.nextInt(1,LIMIT_OBSTACLE_NUMBER+1);
        if(this.isLarge){
            cactus_height = large_cactus_height;
        }else{
            cactus_height = small_cactus_height;
        }
       //this will prevent on generating large cactus with 3 set
        // the dino is too slow to jump
        if(set_of_catus >=LIMIT_OBSTACLE_NUMBER && isLarge && obs_speed_x <7 ){
            set_of_catus = LIMIT_OBSTACLE_NUMBER -1;
        }

        generateCactus(set_of_catus,cactus_height);

    }


    public  void  createBat(){
        isCactus =false;
    int valid_y [] = {5,small_cactus_height,large_cactus_height,large_cactus_height+small_cactus_height};
    obs_y = SCREEN_HEIGHT  -  valid_y[randomGenerator.nextInt(valid_y.length)] - bat_heigth;
    obs_height= bat_heigth;
    obs_width=bat_width;
    obs_x =  SCREEN_WIDTH / Scaled_x;
    animations_frame[bat_anim_frame_ind]= 0;
    }



    public void animateBat(Graphics g2d , int id , int x , int y , BufferedImage [] animation){
        animation_timers[id] += TIMER_DELAY + animation.length ;
        int animation_frame = animations_frame[id];
        g2d.drawImage(animation[animation_frame], x,y,null);
        if(animation_timers[id]  >= MILLISEC_IMAGE_CHANGE  ){
            animation_timers[id]  = 0;
            animation_frame++;
            if(animation_frame >=animation.length)
                animation_frame = 0;
        }
        //set
        animations_frame[id] =animation_frame;
    }


    public void animateDino(Graphics g2d , int id , int x , int y , BufferedImage [] animation){


        animation_timers[id] += TIMER_DELAY + animation.length*2 ;
        int animation_frame = animations_frame[id];
        if(isInTheAir()){
            animation_frame =0;
        }
        g2d.drawImage(animation[animation_frame], x,y,null);
        if(animation_timers[id]  >= MILLISEC_IMAGE_CHANGE  ){
            animation_timers[id]  = 0;
            animation_frame++;
            if(animation_frame >=animation.length)
                animation_frame = isStanding ? 1 : 0;
        }


        //set
        animations_frame[id] =animation_frame;
    }
    public void animateCatus(Graphics g2d){
        double currentX = obs_x;
        for (int i = 0; i < cactus_images_display.length; i++) {
            int cactus_image_width = cactus_images_display[i].getWidth();
            int cactus_image_height = cactus_images_display[i].getHeight();
            double currentY =  SCREEN_HEIGHT - cactus_image_height ;
            g2d.drawImage(cactus_images_display[i],(int)currentX,(int)currentY,null);
            currentX+= cactus_image_width;
        }
    }
    public void drawScore(Graphics g2d){
        char [] score  = String.valueOf(this.Score).toCharArray();
        int x_offset = 10;
        int x = (int)(SCREEN_WIDTH/Scaled_x) - score.length* this.score_pixel_width;
        int y =SCREEN_HEIGHT+score_pixel_height - g2d.getClipBounds().height  ;


        for (int i = 0; i < score.length; i++) {
            int index =  Integer.parseInt(String.valueOf(score[i]));

        g2d.drawImage(score_images[index],x,y,null);
          x+=this.score_pixel_width;
        }




    }





    @Override
    public void addNotify() {
        super.addNotify();
    }
private double Scaled_x = 3;

    public void deathAnimation(Graphics g){

        stand();


        g.drawImage(dino_death_images[0],(int)(dino_x),(int)dino_y,null);

    }


    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;


        int floorOffset = 10;
        g2.scale(Scaled_x,Scaled_x);

        g2.translate(0,- (getSize().height - g2.getClipBounds().height)  - floorOffset);

        drawBackground(g);

        //animation
       drawObstacle(g);
        drawDino( g);
        drawScore(g);



    }
    public  void drawDino(Graphics g2){
        //death
        if(gameStatus == 1){
            deathAnimation(g2);
            return;
        }

        int animation_id;
        if(isStanding){
            animation_id = dino_anim_frame_ind;
            dino_current_animation = dinosaur_images;
        }else{
            animation_id = ducking_anim_frame_ind;
            dino_current_animation = dino_duck_images;
        }

        animateDino(g2,animation_id,(int)dino_x,(int) dino_y,dino_current_animation);

    }
    public void drawBackground(Graphics g){
        sky.drawBackGround(g);
        ground.drawBackGround(g);

    }
    public void drawObstacle(Graphics g2d){
        int x =(int)obs_x;
        int y =(int)obs_y;
    if(isCactus)
        animateCatus(g2d);
    else    animateBat(g2d,bat_anim_frame_ind,x,y,bat_images);



    }




    public  class TAdapter implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {



        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
        if(gameStatus == 1){
            Start();
            return;
        }
            if (key == KeyEvent.VK_SPACE) {
                jump();
            }

            if (key == KeyEvent.VK_DOWN) {
                duck();
            }
        }

        @Override
        public void keyReleased(KeyEvent e){
            int key = e.getKeyCode();
            stand();

        }





    }

    public class background{
         private int x =0 ;
         private  int y ;
      private ArrayList<BufferedImage> background_images = new ArrayList<>();
        private   ArrayList<Integer>  cloud_heights = new ArrayList<>();
        private boolean isLastCloud = false;
        private int width;
        private  int height;
        private  BufferedImage images_set [];
        private Random rand_number =new Random();
        private int validCloudHeights [] = { 0,5,10,15,20 };
        private boolean isGround = false;
    private  int properties [];

        public background(BufferedImage image_set [],int prop[],boolean isGround){
            this.images_set = image_set;
            properties = prop;
            this.isGround =isGround ;
            reset();


        }
        public void  reset(){
            isLastCloud = false;
            background_images.clear();
            cloud_heights.clear();
            this.x = properties[0];
            this.y = properties[1];
            this. width = properties[2];
            this. height = properties[3];

            if(!isGround){
                initializeClouds();
            }else{
                initializeFloor();
            }

        }
        private  void  initializeClouds(){
            int numberOfclouds =  SCREEN_WIDTH / width ;
            for (int i = 0; i <numberOfclouds ; i++) {


                generateCloud();

            }

        }
        public void generateCloud(){
            if(isLastCloud){
                addBlank();
                   return;
            }

            if (Math.random() <=0.8) {
                addBlank();
            } else {
                addCloud();
            }

        }
        private  void addBlank(){
            BufferedImage blank= new BufferedImage(width,height,  BufferedImage.TRANSLUCENT);
            background_images.add(blank);
            cloud_heights.add(validCloudHeights[0]);
            isLastCloud=false;
        }
        private  void addCloud(){
            background_images.add(images_set[0]);
            cloud_heights.add(validCloudHeights[rand_number.nextInt(validCloudHeights.length)]);
            isLastCloud=true;
        }
        private  void generateFloor(){
            background_images.add(images_set[rand_number.nextInt(images_set.length)]);
        }
        private  void  initializeFloor(){
            int numberOfFloor =  SCREEN_WIDTH / width + 1;
            for (int i = 0; i <numberOfFloor ; i++) {
                generateFloor();

            }


        }
        public  void addImage(){
            this.x = 0;
            background_images.remove(0);
            if(isGround)
                generateFloor();
            else {
                cloud_heights.remove(0);
                generateCloud();
            }
        }
        public void moveBackground(double speed){

            x = x -(int) speed;

            if(Math.abs(x) >= width)addImage();

        }
        public void drawBackGround(Graphics g2d){
            int x = (int) this.x;


                for (int i = 0; i < background_images.size(); i++) {
                   int y =  isGround ? (int)this.y : (int) this.y  - cloud_heights.get(i);
                    g2d.drawImage(background_images.get(i),x,y,null);
                    x+= background_images.get(i).getWidth();
                }



        }



    }

}
