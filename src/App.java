import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class App extends JPanel implements MouseListener, MouseMotionListener {
    static Color white = new Color(255, 255, 255); //colour*
    static Color Black = new Color(0, 0, 0);
    static Color Red = new Color(255, 0, 0);

    static Color playerColor1 = new Color(61, 87, 142);
    static Color playerColor2 = new Color(79, 154, 255);
    static Color enemyColor2 = new Color(216, 0, 0);
    static Color enemyColor1 = new Color(103, 0, 0);
    static Color gray = new Color(120, 120, 120);
    static Color darkGray = new Color(46, 46, 46);

    static Color backGround = new Color(209, 209, 209);

    static Color Green = new Color(60, 200, 90);
    static Color Yellow = new Color(240, 200, 50);

    static int windowWidth = 1280;
    static int windowHeight = 720;

    static final double ENEMY_RESPAWN_DELAY = 1.5; // 1.5 seconds
    static final int POINTS_PER_KILL = 100;
    static final int MAX_ENEMIES = 12; // prevents too many
    static final Path HIGH_SCORE_FILE = Paths.get("highscore.txt");

    Player player = new Player(new Vector(windowWidth / 2.0, windowHeight / 2.0));
    ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    ArrayList<Double> pendingRespawns = new ArrayList<Double>();
    ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    Random random = new Random();

    Vector mousePosition = new Vector(windowWidth / 2.0 + 100, windowHeight / 2.0);
    boolean firing = false;

    int kills = 0;
    int score = 0;
    int highScore = 0;
    boolean newHighScore = false;
    boolean gameOver = false;

    long lastFrameTime;

    public App() {
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setBackground(backGround);
        addKeyListener(player);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (gameOver && (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    restart();
                }
            }
        });
        setFocusable(true);
        highScore = loadHighScore();
        spawnEnemy();
    }

    /**
     * Resets the game
     */
    void restart() {
        player.reset(new Vector(windowWidth / 2.0, windowHeight / 2.0));
        bullets.clear();
        enemies.clear();
        pendingRespawns.clear();
        kills = 0;
        score = 0;
        newHighScore = false;
        gameOver = false;
        firing = false;
        spawnEnemy();
    }

    /**
     * Picks a valid spot which is at least 400px away from player
     */
    void spawnEnemy() {
        if (enemies.size() >= MAX_ENEMIES) {
            return;
        }

        Vector spawn = new Vector(60, 60);
        for (int attempt = 0; attempt < 30; attempt++) {
            Vector candidate = new Vector(60 + random.nextDouble() * (windowWidth - 120),
                    60 + random.nextDouble() * (windowHeight - 120));
            if (candidate.subtract(player.getPosition()).length() >= 400) {
                spawn = candidate;
                break;
            }
        }
        enemies.add(new Enemy(spawn, kills)); // each new enemy is stronger than the last
    }

    /**
     * Stops the game and saves potential high score
     */
    void endGame() {
        gameOver = true;
        firing = false;
        if (score > highScore) {
            highScore = score;
            newHighScore = true;
            saveHighScore(highScore);
        }
    }

    /**
     * Opens up high score from highscore.txt
     * if there is an exception, such as file not found, it will instead read 0
     */
    static int loadHighScore() {
        try {
            return Integer.parseInt(Files.readAllLines(HIGH_SCORE_FILE).get(0).trim());
        } catch (IOException | RuntimeException e) {
            return 0; // no file yet (or unreadable): start from zero
        }
    }

    /**
     * when game ends, it is called to write highscore to the save file
     */
    static void saveHighScore(int value) {
        try {
            Files.write(HIGH_SCORE_FILE, String.valueOf(value).getBytes());
        } catch (IOException e) {
            System.out.println("Could not save high score: " + e);
        }
    }

    /**
     * an update function, updates everything in the game which includes mouse position, player, enemies, and bullets
     * @param delta how much time has passed since the last update to maintain correct time
     */
    public void update(double delta) {
        if (!gameOver) {
            player.update(delta);
            player.aimAt(mousePosition);

            if (firing && player.canShoot()) {
                bullets.add(player.shoot(true));
            }

            for (Enemy enemy : enemies) {
                enemy.update(delta, player);
                double distance = enemy.getPosition().subtract(player.getPosition()).length();
                if (enemy.canShoot() && distance < 800) {
                    bullets.add(enemy.shoot(false));
                }
            }

            for (int i = pendingRespawns.size() - 1; i >= 0; i--) {
                double timer = pendingRespawns.get(i) - delta;
                if (timer <= 0) {
                    pendingRespawns.remove(i);
                    spawnEnemy();
                } else {
                    pendingRespawns.set(i, timer);
                }
            }

            updateBullets(delta);

            if (!player.isAlive()) {
                endGame();
            }
        }
        repaint();
    }

    void updateBullets(double delta) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);

            if (!isOnScreen(bullet.getPosition(), 50)) {
                bullets.remove(i);
                continue;
            }

            double hitDistance = Player.RADIUS + Bullet.RADIUS;

            if (bullet.isFromPlayer()) {
                boolean bulletHit = false;
                for (int j = enemies.size() - 1; j >= 0; j--) {
                    Enemy enemy = enemies.get(j);
                    if (bullet.getPosition().subtract(enemy.getPosition()).length() < hitDistance) {
                        enemy.takeDamage(1);
                        bulletHit = true;

                        if (!enemy.isAlive()) {
                            enemies.remove(j);
                            kills++;
                            score += POINTS_PER_KILL;

                            pendingRespawns.add(ENEMY_RESPAWN_DELAY);
                            if(Math.random() < 0.1 || score == 200) { //20 chance to spawn an extra for more challenge or at 200
                                pendingRespawns.add(ENEMY_RESPAWN_DELAY);
                            }

                        }
                        break;
                    }
                }
                if (bulletHit) {
                    bullets.remove(i);
                }
            } else if (!player.isInvulnerable()
                    && bullet.getPosition().subtract(player.getPosition()).length() < hitDistance) {
                player.takeDamage(1);
                bullets.remove(i);
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g; //since it's a 2d graphics, this implies that paint by default is 3d

        for (Bullet bullet : bullets) { //draws all bullets
            g2.setColor(bullet.isFromPlayer() ? playerColor1 : enemyColor1);
            g2.fillOval((int) bullet.getPosition().x - 10, (int) bullet.getPosition().y - 10, 20, 20);
        }

        for (Enemy enemy : enemies) {
            drawTank(g2, enemy, enemyColor1, enemyColor2);
            drawBar(g2, enemy.getPosition().x, enemy.getPosition().y - 45, 60, 8,
                    (double) enemy.getHealth() / enemy.getMaxHealth(), Red);
        }

        drawTank(g2, player, playerColor1, playerColor2);
        // reload bar under the player
        drawBar(g2, player.getPosition().x, player.getPosition().y + 45, 60, 8,
                player.getReloadProgress(), player.canShoot() ? Green : Yellow);

        drawHud(g2);

        if (gameOver) {
            drawGameOver(g2);
        }
    }

    /**
     * Draws tank, both player and enemy
     * @param g2 is the paint component
     * @param p is a human class (which includes enemy)
     * @param bodyColor1 The outer color, the darker of the 2, serves as outline
     * @param bodyColor2 The inner color, lighter of the two
     */
    void drawTank(Graphics2D g2, Player p, Color bodyColor1, Color bodyColor2) {
        // flicker while the player is invulnerable after a hit
        if (p.isInvulnerable() && (int) (p.invulnerableTimer * 12) % 2 == 0) {
            return;
        }
        Vector pos = p.getPosition();
        g2.setColor(darkGray);
        g2.setStroke(new BasicStroke(22));
        g2.drawLine((int) pos.x, (int) pos.y, (int) p.getCannonTip().x, (int) p.getCannonTip().y);
        g2.setColor(gray);
        g2.setStroke(new BasicStroke(12));
        g2.drawLine((int) pos.x, (int) pos.y, (int) p.getCannonTip().x, (int) p.getCannonTip().y);
        g2.setColor(bodyColor1);
        g2.fillOval((int) pos.x - 25, (int) pos.y - 25, 50, 50); //actually paints the circle at the coordinates
        g2.setColor(bodyColor2);
        g2.fillOval((int) pos.x - 20, (int) pos.y - 20, 40, 40); //actually paints the circle at the coordinates
    }

    // A bar centred on (cx, y): dark background, coloured fill, white outline
    void drawBar(Graphics2D g2, double cx, double y, int width, int height, double fraction, Color fill) {
        int x = (int) cx - width / 2;
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Black);
        g2.fillRect(x, (int) y, width, height);
        g2.setColor(fill);
        g2.fillRect(x, (int) y, (int) (width * Math.max(0, Math.min(1, fraction))), height);
        g2.setColor(white);
        g2.drawRect(x, (int) y, width, height);
    }

    /**
     * Draws the health bar, high score and current score, as well as enemy count / incoming status
     * @param g2 the paint component
     */
    void drawHud(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2));

        for (int i = 0; i < player.getMaxHealth(); i++) {
            int x = 20 + i * 36;
            if (i < player.getHealth()) {
                g2.setColor(playerColor2);
                g2.fillOval(x, 20, 28, 28);
            } else {
                g2.setColor(gray);
                g2.drawOval(x, 20, 28, 28);
            }
        }

        g2.setColor(Black);
        g2.drawString("Score: " + score, 20, 80);
        g2.drawString("High score: " + highScore, 20, 108);

        String info = "Enemies: " + enemies.size()
                + (pendingRespawns.isEmpty() ? "" : " (" + pendingRespawns.size() + " incoming)");
        g2.drawString(info, windowWidth - 20 - g2.getFontMetrics().stringWidth(info), 38);
    }

    /**
     * Draws the game over Screen
     * @param g2
     */
    void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, windowWidth, windowHeight);

        g2.setColor(darkGray);
        g2.fillRect(windowWidth / 2 - 200, windowHeight / 2 - 180, 400, 360);
        g2.setColor(gray);
        g2.fillRect(windowWidth / 2 - 190, windowHeight / 2 - 170, 380, 340);
        g2.setColor(white);
        g2.setFont(new Font("Dialog", Font.BOLD, 50));
        drawCentered(g2, "GAME OVER", windowHeight / 2 - 60);

        g2.setFont(new Font("Dialog", Font.BOLD, 20));

        drawCentered(g2, "Score: " + score + "   (enemies defeated: " + kills + ")", windowHeight / 2);
        if (newHighScore) {
            g2.setColor(Yellow);
            drawCentered(g2, "NEW HIGH SCORE!", windowHeight / 2 + 45);
        } else {
            drawCentered(g2, "High score: " + highScore, windowHeight / 2 + 45);
        }

        g2.setColor(white);
        drawCentered(g2, "Press R or Enter to play again", windowHeight / 2 + 100);
    }

    void drawCentered(Graphics2D g2, String text, int y) {
        g2.drawString(text, (windowWidth - g2.getFontMetrics().stringWidth(text)) / 2, y);
    }

    /**
     * game runs at 60fps
     */
    void start() {
        lastFrameTime = System.nanoTime();
        new javax.swing.Timer(1000 / 60, e -> {
            long now = System.nanoTime();
            double delta = Math.min((now - lastFrameTime) / 1_000_000_000.0, 0.05); //every fps (60fps)
            lastFrameTime = now;
            update(delta);
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            JFrame frame = new JFrame("Aim Trainer"); //createsaframe
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //stops the code when you close the window;
            frame.add(app);
            frame.pack(); //sizes the window to the game area
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            app.requestFocusInWindow();
            app.start();
        });
    }

    public static Boolean isOnScreen(Vector position, double leniency) {
        if (position.x + leniency < 0 || position.x - leniency > windowWidth || position.y + leniency < 0 || position.y - leniency > windowHeight) //lenincy is extra space to make sure bullet is gone
            return false;
        return true;
    }

    // ---------------------------------------------------------------- mouse

    public void mousePressed(MouseEvent e) {
        mousePosition = new Vector(e.getX(), e.getY());
        firing = true; // hold the button to keep firing; the reload time limits the rate
    }

    public void mouseReleased(MouseEvent e) {
        firing = false;
    }

    public void mouseMoved(MouseEvent e) {
        mousePosition = new Vector(e.getX(), e.getY());
    }

    public void mouseDragged(MouseEvent e) {
        mousePosition = new Vector(e.getX(), e.getY());
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}