package be.howest.ti.mars.logic.data.repoInterfaces;

import be.howest.ti.mars.logic.controller.accounts.BaseAccount;
import be.howest.ti.mars.logic.controller.converters.ShortEndpoint;

import java.util.Set;

public interface FavoritesRepoInt {
    Set<ShortEndpoint> getFavoriteEndpoints(BaseAccount acc);

    void favoriteEndpoint(BaseAccount acc, int id);

    void unFavoriteEndpoint(BaseAccount user, int id);
}
