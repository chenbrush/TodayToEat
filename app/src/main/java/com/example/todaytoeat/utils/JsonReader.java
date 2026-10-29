package com.example.todaytoeat.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 商铺分组 JSON 文件读写工具
 *
 * 推荐使用以下两种调用方式：
 * 1. readShopGroups(context) 读取全部分组；
 * 2. writeShopGroups(context, groups) 覆盖保存全部分组。
 *
 * 也保留了 readJsonContext/writeJsonContext，用于直接读写原始 JSON 文本。
 */
public class JsonReader {
    /** JSON 文件名称 */
    private static final String FILE_NAME = "shop_groups.json";
    /** 日志标签 */
    private static final String TAG = "JsonReader";

    /** 格式化输出 JSON，方便用户直接查看和编辑 */
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * 获取 JSON 文件对象
     * 使用 File 构造方法拼接目录和文件名，避免漏掉路径分隔符
     */
    private File getJsonFile(Context context) {
        File directory = AppConstantsUtils.getJsonInDataFilePath(context);
        return new File(directory, FILE_NAME);
    }

    /**
     * 读取 JSON 原始文本
     *
     * @return 文件不存在时返回空字符串
     */
    public String readJsonContext(Context context) {
        File jsonFile = getJsonFile(context);
        if (!jsonFile.exists()) {
            Log.d(TAG, "JSON 文件不存在：" + jsonFile.getAbsolutePath());
            return "";
        }

        // 读取过程中不修改文件，避免把用户数据清空
        String json = FileUtil.openText(jsonFile.getAbsolutePath());
        Log.d(TAG, "readJsonContext: " + json);
        return json;
    }

    /**
     * 写入 JSON 原始文本，会覆盖原文件
     *
     * @return true 表示写入成功
     */
    public boolean writeJsonContext(Context context, String json) {
        File jsonFile = getJsonFile(context);
        boolean saved = FileUtil.saveText(
                jsonFile.getAbsolutePath(), json == null ? "" : json);
        if (!saved) {
            Log.e(TAG, "写入 JSON 失败：" + jsonFile.getAbsolutePath());
        }
        return saved;
    }

    /**
     * 读取全部分组
     *
     * 文件不存在或内容为空时返回空列表，并创建一个空 JSON 数组；
     * JSON 格式错误时只记录日志并返回空列表，不会覆盖原文件。
     */
    public List<ShopGroupData> readShopGroups(Context context) {
        File jsonFile = getJsonFile(context);
        String json = readJsonContext(context);
        if (json == null || json.trim().isEmpty()) {
            // 文件存在但读取结果为空，可能是读取失败，不能直接覆盖原文件
            if (jsonFile.exists() && jsonFile.length() > 0) {
                Log.e(TAG, "JSON 文件读取失败或内容为空：" + jsonFile.getAbsolutePath());
                return new ArrayList<>();
            }
            List<ShopGroupData> empty = new ArrayList<>();
            writeShopGroups(context, empty);
            return empty;
        }

        try {
            // 兼容带 BOM 的文件，避免第一个字符导致解析失败
            if (json.startsWith("\uFEFF")) {
                json = json.substring(1);
            }
            return normalizeGroups(parseGroups(json));
        } catch (JsonSyntaxException | IllegalStateException e) {
            Log.e(TAG, "商铺分组 JSON 格式错误", e);
            return new ArrayList<>();
        }
    }

    /**
     * 覆盖保存全部分组
     *
     * @param groups 分组列表，传 null 时写入空数组
     * @return true 表示写入成功
     */
    public boolean writeShopGroups(Context context, List<ShopGroupData> groups) {
        List<ShopGroupData> normalized = normalizeGroups(groups);
        return writeJsonContext(context, gson.toJson(normalized));
    }

    /**
     * 新增分组；如果分组名已存在，则把商铺合并到原分组
     * 同一个商铺只会保留在一个分组中，加入新分组时会从其它分组移除。
     *
     * @return true 表示保存成功
     */
    public boolean addShopGroup(Context context, String groupName, String... shopNames) {
        String name = groupName == null ? "" : groupName.trim();
        if (name.isEmpty()) {
            return false;
        }

        List<ShopGroupData> groups = readShopGroups(context);
        ShopGroupData group = findGroup(groups, name);
        if (group == null) {
            group = new ShopGroupData(name, new ArrayList<>());
            groups.add(group);
        }

        if (shopNames != null) {
            for (String shopName : shopNames) {
                if (shopName == null || shopName.trim().isEmpty()) {
                    continue;
                }
                String shop = shopName.trim();
                // 保证一个商铺只属于一个分组
                removeShopFromOtherGroups(groups, group, shop);
                if (!group.getGroupShopsList().contains(shop)) {
                    group.getGroupShopsList().add(shop);
                }
            }
        }

        return writeShopGroups(context, groups);
    }

    /**
     * 把单个商铺加入指定分组；分组不存在时会自动创建
     *
     * @return true 表示保存成功
     */
    public boolean addShopToGroup(Context context, String groupName, String shopName) {
        return addShopGroup(context, groupName, shopName);
    }

    /**
     * 根据商铺名称获取所属分组
     *
     * @param shopName 商铺名称
     * @return 所属分组；商铺未分组或名称为空时返回 null
     */
    public ShopGroupData getGroupByShopName(Context context, String shopName) {
        String targetShop = shopName == null ? "" : shopName.trim();
        if (targetShop.isEmpty()) {
            return null;
        }

        List<ShopGroupData> groups = readShopGroups(context);
        for (ShopGroupData group : groups) {
            if (group.getGroupShopsList().contains(targetShop)) {
                return group;
            }
        }
        return null;
    }

    /**
     * 根据商铺名称获取所属分组名称
     *
     * @return 分组名称；商铺未分组时返回空字符串
     */
    public String getGroupNameByShopName(Context context, String shopName) {
        ShopGroupData group = getGroupByShopName(context, shopName);
        if (group == null || group.getGroupName() == null) {
            return "";
        }
        return group.getGroupName();
    }

    /**
     * 根据分组名称查找分组
     *
     * @param groupName 分组名称
     * @return 找到的分组；不存在时返回 null
     */
    public ShopGroupData getGroupByName(Context context, String groupName) {
        String name = groupName == null ? "" : groupName.trim();
        if (name.isEmpty()) {
            return null;
        }

        List<ShopGroupData> groups = readShopGroups(context);
        for (ShopGroupData group : groups) {
            if (name.equals(group.getGroupName())) {
                return group;
            }
        }
        return null;
    }

    /**
     * 兼容旧调用：新增或合并分组
     *
     * @return true 表示保存成功
     */
    public boolean writeShopGroupAndName(Context context, String groupName, String... groupShopNames) {
        return addShopGroup(context, groupName, groupShopNames);
    }

    /**
     * 修改分组名称
     *
     * 新名称不能为空，也不能与其它分组重名，否则返回 false。
     *
     * @param oldGroupName 原分组名称
     * @param newGroupName 新分组名称
     * @return true 表示保存成功
     */
    public boolean renameShopGroup(Context context, String oldGroupName, String newGroupName) {
        String oldName = oldGroupName == null ? "" : oldGroupName.trim();
        String newName = newGroupName == null ? "" : newGroupName.trim();
        if (oldName.isEmpty() || newName.isEmpty()) {
            return false;
        }

        List<ShopGroupData> groups = readShopGroups(context);
        ShopGroupData target = findGroup(groups, oldName);
        if (target == null) {
            return false;
        }

        // 名称没有变化时直接视为成功
        if (oldName.equals(newName)) {
            return true;
        }

        // 新名称已被其它分组使用，避免误合并
        if (findGroup(groups, newName) != null) {
            return false;
        }

        target.setGroupName(newName);
        return writeShopGroups(context, groups);
    }

    /**
     * 更新分组内的商铺（覆盖原有商铺列表）
     *
     * 一个商铺只保留在一个分组中，加入本分组时会从其它分组移除；
     * 更新后没有商铺时，删除该分组。
     *
     * @return true 表示保存成功
     */
    public boolean updateShopGroup(Context context, String groupName, String... shopNames) {
        String name = groupName == null ? "" : groupName.trim();
        if (name.isEmpty()) {
            return false;
        }

        List<ShopGroupData> groups = readShopGroups(context);
        ShopGroupData group = findGroup(groups, name);
        if (group == null) {
            return false;
        }

        List<String> shops = new ArrayList<>();
        if (shopNames != null) {
            for (String shopName : shopNames) {
                if (shopName == null || shopName.trim().isEmpty()) {
                    continue;
                }
                String shop = shopName.trim();
                // 保证一个商铺只属于一个分组
                removeShopFromOtherGroups(groups, group, shop);
                if (!shops.contains(shop)) {
                    shops.add(shop);
                }
            }
        }

        // 编辑后没有商铺时，直接删除该分组
        if (shops.isEmpty()) {
            groups.remove(group);
        } else {
            group.setGroupShopsList(shops);
        }
        return writeShopGroups(context, groups);
    }

    /**
     * 删除指定分组
     *
     * @param groupName 分组名称
     * @return true 表示保存成功；分组不存在时同样返回 true
     */
    public boolean deleteShopGroup(Context context, String groupName) {
        String name = groupName == null ? "" : groupName.trim();
        if (name.isEmpty()) {
            return false;
        }

        List<ShopGroupData> groups = readShopGroups(context);
        ShopGroupData group = findGroup(groups, name);
        if (group == null) {
            // 分组本来就不存在，视为删除成功
            return true;
        }

        groups.remove(group);
        return writeShopGroups(context, groups);
    }

    /**
     * 解析分组数据，兼容三种 JSON 格式：
     * 1. [{ "groupName": "...", "groupShopsList": [...] }]
     * 2. { "groups": [{ "groupName": "...", "groupShopsList": [...] }] }
     * 3. { "groupName": "...", "groupShopsList": [...] }
     */
    private List<ShopGroupData> parseGroups(String json) {
        JsonElement element = JsonParser.parseString(json);
        Type listType = new TypeToken<List<ShopGroupData>>() {
        }.getType();

        if (element.isJsonArray()) {
            List<ShopGroupData> groups = gson.fromJson(element, listType);
            return groups == null ? new ArrayList<>() : groups;
        }

        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("groups") && object.get("groups").isJsonArray()) {
                List<ShopGroupData> groups = gson.fromJson(object.get("groups"), listType);
                return groups == null ? new ArrayList<>() : groups;
            }
            if (object.has("groupName")) {
                ShopGroupData group = gson.fromJson(object, ShopGroupData.class);
                List<ShopGroupData> groups = new ArrayList<>();
                if (group != null) {
                    groups.add(group);
                }
                return groups;
            }
        }

        return new ArrayList<>();
    }

    /**
     * 清理空分组、空商铺和重复数据
     */
    private List<ShopGroupData> normalizeGroups(List<ShopGroupData> groups) {
        List<ShopGroupData> result = new ArrayList<>();
        if (groups == null) {
            return result;
        }

        for (ShopGroupData group : groups) {
            if (group == null) {
                continue;
            }

            String groupName = group.getGroupName() == null
                    ? ""
                    : group.getGroupName().trim();
            if (groupName.isEmpty()) {
                continue;
            }

            List<String> shops = new ArrayList<>();
            if (group.getGroupShopsList() != null) {
                for (String shopName : group.getGroupShopsList()) {
                    if (shopName == null || shopName.trim().isEmpty()) {
                        continue;
                    }
                    String shop = shopName.trim();
                    if (!shops.contains(shop)) {
                        shops.add(shop);
                    }
                }
            }

            ShopGroupData existing = findGroup(result, groupName);
            if (existing == null) {
                group.setGroupName(groupName);
                group.setGroupShopsList(shops);
                result.add(group);
            } else {
                // 同名分组自动合并
                for (String shop : shops) {
                    if (!existing.getGroupShopsList().contains(shop)) {
                        existing.getGroupShopsList().add(shop);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 根据分组名查找分组
     */
    private ShopGroupData findGroup(List<ShopGroupData> groups, String groupName) {
        for (ShopGroupData group : groups) {
            if (groupName.equals(group.getGroupName())) {
                return group;
            }
        }
        return null;
    }

    /**
     * 从其它分组中移除指定商铺，并清理因此变空的分组
     */
    private void removeShopFromOtherGroups(List<ShopGroupData> groups,
                                           ShopGroupData targetGroup,
                                           String shopName) {
        for (int i = groups.size() - 1; i >= 0; i--) {
            ShopGroupData group = groups.get(i);
            if (group == targetGroup) {
                continue;
            }

            group.getGroupShopsList().remove(shopName);
            if (group.getGroupShopsList().isEmpty()) {
                groups.remove(i);
            }
        }
    }
}
