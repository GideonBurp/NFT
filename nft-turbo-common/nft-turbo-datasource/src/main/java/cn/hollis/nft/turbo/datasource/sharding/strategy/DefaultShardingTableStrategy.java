package cn.hollis.nft.turbo.datasource.sharding.strategy;

/**
 * @author Hollis
 */
public class DefaultShardingTableStrategy implements ShardingTableStrategy {

    public DefaultShardingTableStrategy() {
    }

    @Override
    public int getTable(String externalId,int tableCount) {
        int hashCode = externalId.hashCode();
        return (int) Math.abs((long) hashCode) % tableCount;
    }
}
