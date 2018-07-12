from gb.hypergraph import *
from gb.inference.rules import Rules


rules = Rules()


predicates = ['says/nlp.say.verb', 'is/nlp.be.verb', 'kills/nlp.kill.verb', 'killed/nlp.kill.verb',
              'warns/nlp.warn.verb', 'calls/nlp.call.verb', 'said/nlp.say.verb', 'say/nlp.say.verb',
              'found/nlp.find.verb', 'arrested/nlp.arrest.verb', 'kill/nlp.kill.verb', 'are/nlp.be.verb',
              'dies/nlp.die.verb', 'hits/nlp.hit.verb', 'accuses/nlp.accuse.verb', 'bans/nlp.ban.verb',
              'has/nlp.have.verb', 'rejects/nlp.reject.verb', 'gets/nlp.get.verb', 'accused/nlp.accuse.verb',
              'finds/nlp.find.verb', 'faces/nlp.face.verb', 'seeks/nlp.seek.verb', 'approves/nlp.approve.verb',
              'launches/nlp.launch.verb', 'hit/nlp.hit.verb', 'threatens/nlp.threaten.verb', 'shows/nlp.show.verb',
              'was/nlp.be.verb', 'makes/nlp.make.verb', 'Õs/nlp.Õs.part', 'takes/nlp.take.verb',
              'announces/nlp.announce.verb', 'claims/nlp.claim.verb', 'wins/nlp.win.verb',
              'sentenced/nlp.sentence.verb', 'reveals/nlp.reveal.verb', 'condemns/nlp.condemn.verb',
              'confirms/nlp.confirm.verb', 'declares/nlp.declare.verb', 'gives/nlp.give.verb', 'slams/nlp.slam.verb',
              'offers/nlp.offer.verb', 'sends/nlp.send.verb', 'denies/nlp.deny.verb',
              ('says/nlp.say.verb', 'is/nlp.be.verb'), 'sees/nlp.see.verb', 'urges/nlp.urge.verb',
              'wants/nlp.want.verb', 'opens/nlp.open.verb', 'raises/nlp.raise.verb', 'suspends/nlp.suspend.verb',
              'jailed/nlp.jail.verb', 'arrest/nlp.arrest.verb', 'passes/nlp.pass.verb', 'take/nlp.take.verb',
              'shot/nlp.shoot.verb', 'goes/nlp.go.verb', 'begins/nlp.begin.verb', 'charged/nlp.charge.verb',
              'blames/nlp.blame.verb', 'arrests/nlp.arrest.verb', 'plans/nlp.plan.verb', 'find/nlp.find.verb',
              'becomes/nlp.become.verb', 'seize/nlp.seize.verb', 'reported/nlp.report.verb',
              'defends/nlp.defend.verb', 'leaves/nlp.leave.verb', 'holds/nlp.hold.verb', 'releases/nlp.release.verb',
              'loses/nlp.lose.verb', 'backs/nlp.back.verb', 'rises/nlp.rise.verb', 'have/nlp.have.verb',
              'detained/nlp.detain.verb', 'clash/nlp.clash.verb', 'puts/nlp.put.verb', 'attacked/nlp.attack.verb',
              'call/nlp.call.verb', 'tells/nlp.tell.verb', 'turns/nlp.turn.verb', 'face/nlp.face.verb',
              'admits/nlp.admit.verb', 'unveils/nlp.unveil.verb', 'seizes/nlp.seize.verb', 'comes/nlp.come.verb',
              'strikes/nlp.strike.verb', 'agree/nlp.agree.verb', 'executes/nlp.execute.verb', 'warn/nlp.warn.verb',
              'set/nlp.set.verb', 'injured/nlp.injure.verb', 'show/nlp.show.verb', 'demands/nlp.demand.verb',
              'fired/nlp.fire.verb', 'meets/nlp.meet.verb', 'protest/nlp.protest.verb', 'cancels/nlp.cancel.verb',
              "'s/nlp.have.verb"]


rules.globals['predicates'] = predicates


@rules.add_rule('(:predicates * * ...)')
def claim(_, edge, output):
    output.create(('test/gb', edge[1], edge[2]))


if __name__ == '__main__':
    hg = init_hypergraph('reddit-worldnews-01012013-01082017-new.hg')
    rules.apply_to(hg)
