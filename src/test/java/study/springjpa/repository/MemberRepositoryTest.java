package study.springjpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.springjpa.dto.MemberDto;
import study.springjpa.entity.Member;
import study.springjpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;


    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member saveMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(saveMember.getId()).get();
        Assertions.assertThat(findMember.getId()).isEqualTo(saveMember.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(saveMember.getUsername());
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);

        // 변경 검증
        Member member3 = new Member("member3");
        Member member4 = new Member("member4");

        memberRepository.save(member3);
        memberRepository.save(member4);

        member3.setUsername("한경수");
        Member findMember3 = memberRepository.findById(member3.getId()).get();
        assertThat(findMember3.getUsername()).isEqualTo("한경수");
    }

    @Test
    public void findByUsernameAndAgeGreaterThen() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("aaa", 5);
        assertThat(result.get(0).getUsername()).isEqualTo("aaa");
        assertThat(result.get(0).getAge()).isEqualTo(10);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("aaa", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        m1.setTeam(team);
        m2.setTeam(team);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<MemberDto> result = memberRepository.findMemberDto();
        assertThat(result.size()).isEqualTo(2);
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findbynames() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("aaa", "bbb"));
        assertThat(result.size()).isEqualTo(2);
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void pageing() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> page = memberRepository.findByAge(age, pageRequest);
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(page.getSize()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);
        // 벌크 연산을 한 후에는 flush, clear 처리를 해줘야 한다.
        //em.flush();
        //em.clear();

        List<Member> result = memberRepository.findUser("member5", 41);
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //List<Member> members = memberRepository.findMemberFetchJoin();
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member = " + member.getTeam().getClass());
            System.out.println("member = " + member.getTeam().getName());
        }
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();

    }

}