package cn.gideon.nft.turbo.inventory.exception;

import cn.gideon.nft.turbo.base.exception.ErrorCode;

/**
 * @author Gideon
 */
public enum InventoryErrorCode implements ErrorCode {

    /**
     * 库存更新失败
     */
    INVENTORY_UPDATE_FAILED("INVENTORY_UPDATE_FAILED", "库存更新失败");

    private String code;

    private String message;

    InventoryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
