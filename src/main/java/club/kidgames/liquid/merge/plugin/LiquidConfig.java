package club.kidgames.liquid.merge.plugin;

import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class LiquidConfig {
  private final JavaPlugin plugin;
  private final Logger logger;

  public LiquidConfig(JavaPlugin plugin) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();
  }


}
