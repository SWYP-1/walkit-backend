package com.walkit.walkit.domain.item.service;

import com.walkit.walkit.common.image.entity.CharacterWearImage;
import com.walkit.walkit.common.image.repository.CharacterWearImageRepository;
import com.walkit.walkit.domain.character.entity.Character;
import com.walkit.walkit.domain.character.enums.Grade;
import com.walkit.walkit.domain.character.repository.CharacterWearRepository;
import com.walkit.walkit.domain.item.dto.request.RequestBuyDto;
import com.walkit.walkit.domain.item.dto.response.ResponseItemDto;
import com.walkit.walkit.domain.item.dto.response.ResponseMyItemDto;
import com.walkit.walkit.domain.item.entity.Item;
import com.walkit.walkit.domain.item.entity.ItemManagement;
import com.walkit.walkit.domain.item.enums.Position;
import com.walkit.walkit.domain.item.enums.Tag;
import com.walkit.walkit.domain.item.repository.ItemManagementRepository;
import com.walkit.walkit.domain.item.repository.ItemRepository;
import com.walkit.walkit.domain.user.entity.User;
import com.walkit.walkit.domain.user.repository.UserRepository;
import com.walkit.walkit.global.exception.CustomException;
import com.walkit.walkit.global.exception.ErrorCode;
import com.walkit.walkit.global.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemManagementRepository itemManagementRepository;
    private final CharacterWearRepository characterWearRepository;
    private final CharacterWearImageRepository characterWearImageRepository;

    public void buy(Long userId, RequestBuyDto dto) {
        User user = findUser(userId);

        checkPurchase(user, dto);
        purchaseItem(user, dto);
    }

    public List<ResponseItemDto> findAll(UserPrincipal userPrincipal, Position position) {
        List<ResponseItemDto> responseItemDtos = new ArrayList<>();

        if (position == null) {
            for (Item item : itemRepository.findAll()) {
                boolean isOwned = isOwned(userPrincipal, item);
                boolean isWorn = isWorn(userPrincipal, item);
                ResponseItemDto responseItemDto = createResponseItemDtoByUser(userPrincipal, item, isOwned, isWorn);
                responseItemDtos.add(responseItemDto);
            }

            return responseItemDtos;
        } else {
            for (Item item : itemRepository.findAllByPosition(position)) {
                boolean isOwned = isOwned(userPrincipal, item);
                boolean isWorn = isWorn(userPrincipal, item);

                ResponseItemDto responseItemDto = createResponseItemDtoByUser(userPrincipal, item, isOwned, isWorn);
                responseItemDtos.add(responseItemDto);
            }

            return responseItemDtos;
        }
    }

    private static ResponseItemDto createResponseItemDto(Item item, boolean isOwned, boolean isWorn) {
        ResponseItemDto responseItemDto = ResponseItemDto.from(item, isOwned, isWorn);
        return responseItemDto;
    }

    private ResponseItemDto createResponseItemDtoByUser(UserPrincipal userPrincipal, Item item, boolean isOwned, boolean isWorn) {
        if (item.getPosition() == Position.HEAD) {
            if (userPrincipal != null) {
                User user = userRepository.findById(userPrincipal.getUserId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                Grade grade = user.getCharacter().getGrade();

                Tag tag = null;
                List<CharacterWearImage> characterWearImages = characterWearImageRepository.findByItemNameAndGradeAndPosition(item.getItemName(), grade, Position.HEAD);
                if (!characterWearImages.isEmpty()) {
                    tag = characterWearImages.get(0).getTag();
                }

                return ResponseItemDto.from(item, isOwned, isWorn, tag);
            }
            return ResponseItemDto.from(item, isOwned, isWorn);
        } else {
            return ResponseItemDto.from(item, isOwned, isWorn);
        }
    }

    private boolean isWorn(UserPrincipal userPrincipal, Item item) {
        boolean isWorn = false;

        if (userPrincipal != null) {
            User user = findUser(userPrincipal.getUserId());
            Character character = user.getCharacter();

            if (characterWearRepository.existsByCharacterAndItem(character, item)) {
                isWorn = true;
            }
        }
        return isWorn;
    }

    private boolean isWorn(Long userId, Item item) {
        boolean isWorn = false;

        User user = findUser(userId);
        Character character = user.getCharacter();

        if (characterWearRepository.existsByCharacterAndItem(character, item)) {
            isWorn = true;
        }

        return isWorn;
    }

    public List<ResponseMyItemDto> findItemsByUser(Long userId, Position position) {
        User user = findUser(userId);

        List<ResponseMyItemDto> responseMyItemDtos = new ArrayList<>();

        for (ItemManagement itemManagement : user.getItemManagements()) {
            Item item = itemManagement.getItem();
            if (position == null) {
                boolean isWorn = isWorn(userId, item);
                ResponseMyItemDto responseMyItemDto = createMyItemDtoByTag(user, item, isWorn);
                responseMyItemDtos.add(responseMyItemDto);
            } else {
                if (item.getPosition().equals(position)) {
                    boolean isWorn = isWorn(userId, item);
                    ResponseMyItemDto responseMyItemDto = createMyItemDtoByTag(user, item, isWorn);
                    responseMyItemDtos.add(responseMyItemDto);
                }
            }
        }

        return responseMyItemDtos;
    }

    private static ResponseMyItemDto createMyItemDto(Item item, boolean isWorn) {
        ResponseMyItemDto responseMyItemDto = ResponseMyItemDto.from(item, isWorn);
        return responseMyItemDto;
    }

    private ResponseMyItemDto createMyItemDtoByTag(User user, Item item, boolean isWorn) {
        if (item.getPosition() == Position.HEAD) {
            Grade grade = user.getCharacter().getGrade();

            Tag tag = null;
            List<CharacterWearImage> characterWearImages = characterWearImageRepository.findByItemNameAndGradeAndPosition(item.getItemName(), grade, Position.HEAD);
            if (!characterWearImages.isEmpty()) {
                tag = characterWearImages.get(0).getTag();
            }
            return ResponseMyItemDto.from(item, isWorn, tag);
        } else {
            return ResponseMyItemDto.from(item, isWorn);
        }
    }

    private void checkPurchase(User user, RequestBuyDto dto) {
        if (user.getPoint() < dto.getTotalPrice()) {
            throw new CustomException(ErrorCode.INSUFFICIENT_FUNDS);
        }

        for (RequestBuyDto.BuyItemDto item : dto.getItems()) {
            Long itemId = item.getItemId();

            Item findItem = itemRepository.findById(itemId).orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

            if (itemManagementRepository.existsByUserAndItem(user, findItem)) {
                throw new CustomException(ErrorCode.ALREADY_ITEM_OWNED);
            }
        }
    }

    private boolean isOwned(UserPrincipal userPrincipal, Item item) {
        boolean isOwned = false;

        if (userPrincipal != null) {
            User user = findUser(userPrincipal.getUserId());

            if (itemManagementRepository.existsByUserAndItem(user, item)) {
                isOwned = true;
            }
        }
        return isOwned;
    }

    private void purchaseItem(User user, RequestBuyDto dto) {

        for (RequestBuyDto.BuyItemDto item : dto.getItems()) {
            Item findItem = itemRepository.findById(item.getItemId()).orElseThrow(() -> new CustomException(ErrorCode.ITEM_NOT_FOUND));

            ItemManagement itemManagement = ItemManagement.from(user, findItem);
            itemManagementRepository.save(itemManagement);

            user.minusPoints(findItem.getPoint());

            findItem.plusSaleCount();
        }
    }

    private User findUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user;
    }

    public String findBackGroudImage() {
        // todo 온도를 파라미터로 받고 그에 따른 배경 이미지 리턴
        return null;
    }

}
