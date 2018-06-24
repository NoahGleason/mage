package mage.cards.r;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ExileTargetEffect;
import mage.abilities.effects.common.continuous.AssignNoCombatDamageSourceEffect;
import mage.cards.Card;
import mage.constants.*;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.filter.FilterCard;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.other.OwnerIdPredicate;
import mage.filter.predicate.permanent.ControllerIdPredicate;
import mage.game.Game;
import mage.game.combat.CombatGroup;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetCardInASingleGraveyard;

/**
 *
 * @author noahg
 */
public final class RysorianBadger extends CardImpl {

    public RysorianBadger(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{G}");
        
        this.subtype.add(SubType.BADGER);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Whenever Rysorian Badger attacks and isn't blocked, you may exile up to two target creature cards from defending player's graveyard. If you do, you gain 1 life for each card exiled this way and Rysorian Badger assigns no combat damage this turn.
        this.addAbility(new RysorianBadgerTriggeredAbility());
    }

    public RysorianBadger(final RysorianBadger card) {
        super(card);
    }

    @Override
    public RysorianBadger copy() {
        return new RysorianBadger(this);
    }
}

class RysorianBadgerTriggeredAbility extends TriggeredAbilityImpl {

    public RysorianBadgerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new RysorianBadgerEffect(), true );
        this.addEffect(new AssignNoCombatDamageSourceEffect(Duration.EndOfTurn, true));
    }

    public RysorianBadgerTriggeredAbility(final RysorianBadgerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public RysorianBadgerTriggeredAbility copy() {
        return new RysorianBadgerTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DECLARED_BLOCKERS;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Permanent sourcePermanent = game.getPermanent(getSourceId());
        if (sourcePermanent.isAttacking()) {
            for (CombatGroup combatGroup: game.getCombat().getGroups()) {
                if (combatGroup.getBlockers().isEmpty() && combatGroup.getAttackers().contains(getSourceId())) {
                    UUID defendingPlayerId = game.getCombat().getDefendingPlayerId(getSourceId(), game);
                    FilterCard filter = new FilterCreatureCard();
                    filter.add(new OwnerIdPredicate(defendingPlayerId));
                    this.getTargets().clear();
                    Target target = new TargetCardInASingleGraveyard(0, 2, filter);
                    this.addTarget(target);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever {this} attacks and isn't blocked, you may exile up to two target creature cards from defending player's graveyard. If you do, you gain 1 life for each card exiled this way and {this} assigns no combat damage this turn.";
    }
}

class RysorianBadgerEffect extends OneShotEffect {

    public RysorianBadgerEffect() {
        super(Outcome.Exile);
        staticText = "exile up to two target creature cards from defending player's graveyard. If you do, you gain 1 life for each card exiled this way";
    }

    public RysorianBadgerEffect(final RysorianBadgerEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player player = game.getPlayer(source.getControllerId());
        if (player != null) {
            int exiledCards = 0;
            for (UUID uuid : getTargetPointer().getTargets(game, source)){
                Card toExile = game.getCard(uuid);
                if (toExile != null){
                    toExile.moveToExile(null, "", source.getSourceId(), game);
                    exiledCards++;
                }
            }
            player.gainLife(exiledCards, game, source);
            return true;
        }
        return false;
    }

    @Override
    public RysorianBadgerEffect copy() {
        return new RysorianBadgerEffect(this);
    }

}