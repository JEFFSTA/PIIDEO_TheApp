package ru.crew.motley.piideo.registration;

import ru.crew.motley.piideo.network.Member;

/**
 * Created by vas on 12/18/17.
 */

public interface RegistrationListener {

    void onNextStep(Member member);

    void onComplete();
}
