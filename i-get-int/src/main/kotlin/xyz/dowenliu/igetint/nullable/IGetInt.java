package xyz.dowenliu.igetint.nullable;

import org.jetbrains.annotations.Nullable;

/**
 * <p>create at 2019/10/22</p>
 *
 * @author liufl
 */
public interface IGetInt {
    String get(int i);

    String get(@Nullable Integer i);
}
